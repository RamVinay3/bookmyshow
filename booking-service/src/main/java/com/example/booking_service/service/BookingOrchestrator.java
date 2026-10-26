package com.example.booking_service.service;

import com.example.booking_service.dto.request.SeatLockRequest;
import com.example.booking_service.dto.response.SeatLockResponse;
import com.example.booking_service.dto.response.ShowSeatResponse;
import com.example.booking_service.entity.SeatStatus;
import com.example.booking_service.entity.ShowSeat;
import com.example.booking_service.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class BookingOrchestrator {

    private final BookingService bookingService;
    private final WalletClient walletClient;
    private final ShowSeatRepository seatRepository;


    public BookingResponse bookSeats(SeatLockRequest request) {

        Long walletUserId = parseUserId(request.userId());

        // 1. Lock seats. This is a separate DB transaction.
        SeatLockResponse lock = bookingService.lockSeats(request);

        BigDecimal total;

        // 2. Verify the locks and calculate the total from DB prices.
        try {
            total = bookingService.calculateTotalPrice(
                    lock.showId(),
                    lock.seatIds(),
                    request.userId(),
                    lock.bookingId()
            );
        } catch (RuntimeException ex) {
            bookingService.releaseBookingLocks(
                    lock.showId(),
                    lock.seatIds(),
                    request.userId(),
                    lock.bookingId()
            );
            throw ex;
        }

        // 3. Debit the wallet.
        try {
            walletClient.debit(
                    walletUserId,
                    total,
                    lock.bookingId()
            );
        } catch (WebClientResponseException ex) {

            /*
             * A 4xx response normally means the wallet rejected the request.
             * For this example, release the locks on a 4xx response.
             *
             * A timeout or 5xx is different: the wallet may have committed
             * the debit even though we did not receive a successful response.
             */
            if (ex.getStatusCode().is4xxClientError()) {
                bookingService.releaseBookingLocks(
                        lock.showId(),
                        lock.seatIds(),
                        request.userId(),
                        lock.bookingId()
                );

                throw new ResponseStatusException(
                        HttpStatus.PAYMENT_REQUIRED,
                        "Wallet rejected the debit. Booking was not confirmed.",
                        ex
                );
            }

            // Unknown debit outcome: do not confirm seats or release the lock.
            // The lock can expire while a recovery process checks the wallet.
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not determine the wallet debit outcome. "
                            + "Retry or reconcile using the same booking ID.",
                    ex
            );

        } catch (RuntimeException ex) {
            /*
             * Network errors and unexpected failures may have an unknown
             * debit outcome. Do not assume that no money was deducted.
             */
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Wallet debit outcome is unknown. "
                            + "Do not retry with a new booking ID.",
                    ex
            );
        }

        // 4. Wallet debit succeeded. Confirm seats in one DB transaction.
        try {
            bookingService.confirmBooking(
                    lock.bookingId(),
                    lock.showId(),
                    lock.seatIds()
            );

        } catch (RuntimeException confirmationFailure) {

            // 5. Compensation: refund the successful debit.
            try {
                walletClient.refund(
                        walletUserId,
                        total,
                        lock.bookingId()
                );

                // Release only after the refund has succeeded.
                bookingService.releaseBookingLocks(
                        lock.showId(),
                        lock.seatIds(),
                        request.userId(),
                        lock.bookingId()
                );

            } catch (RuntimeException refundFailure) {

                /*
                 * Do not claim the refund succeeded.
                 * Keep the failure visible for recovery/reconciliation.
                 * The seat lock will eventually expire, but the refund
                 * must be retried using this same booking ID.
                 */
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Seat confirmation failed and refund could not "
                                + "be confirmed. Booking ID: "
                                + lock.bookingId(),
                        refundFailure
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat confirmation failed. Wallet debit was refunded.",
                    confirmationFailure
            );
        }

        // 6. Everything succeeded.
        return new BookingResponse(
                lock.bookingId(),
                lock.showId(),
                lock.seatIds(),
                total,
                "CONFIRMED"
        );
    }
    public BookingResponse initiatePayment(String bookingId) {

        // 1. Retrieve the seats associated with this booking.
        List<ShowSeat> seats = seatRepository.findByBookingId(bookingId);

        if (seats.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found"
            );
        }

        ShowSeat firstSeat = seats.get(0);

        Long showId = firstSeat.getShowId();
        String userId = firstSeat.getLockedBy();

        List<Long> seatIds = seats.stream()
                .map(ShowSeat::getSeatId)
                .toList();

        // 2. Handle a retry after the booking was already confirmed.
        if (seats.stream().allMatch(
                seat -> seat.getStatus() == SeatStatus.BOOKED)) {

            BigDecimal total = seats.stream()
                    .map(ShowSeat::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new BookingResponse(
                    bookingId, showId, seatIds, total, "CONFIRMED"
            );
        }

        // 3. Ensure every seat is still locked for this booking.
        LocalDateTime now = LocalDateTime.now();

        boolean valid = seats.stream().allMatch(seat ->
                seat.getStatus() == SeatStatus.LOCKED
                        && Objects.equals(seat.getBookingId(), bookingId)
                        && Objects.equals(seat.getShowId(), showId)
                        && Objects.equals(seat.getLockedBy(), userId)
                        && seat.getLockedUntil() != null
                        && seat.getLockedUntil().isAfter(now)
        );

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat locks are invalid or expired"
            );
        }

        // 4. Calculate the amount from the locked seats in the database.
        BigDecimal total = bookingService.calculateTotalPrice(
                showId, seatIds, userId, bookingId
        );

        Long walletUserId = parseUserId(userId);

        // 5. Debit the wallet.
        try {
            walletClient.debit(walletUserId, total, bookingId);
        } catch (WebClientResponseException ex) {

            if (ex.getStatusCode().is4xxClientError()) {
                bookingService.releaseBookingLocks(
                        showId, seatIds, userId, bookingId
                );

                throw new ResponseStatusException(
                        HttpStatus.PAYMENT_REQUIRED,
                        "Wallet rejected the payment",
                        ex
                );
            }

            // The debit outcome may be unknown.
            // Keep the lock and reconcile using the same booking ID.
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment outcome is unknown; reconcile before retrying",
                    ex
            );
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment outcome is unknown; reconcile before retrying",
                    ex
            );
        }

        // 6. Confirm the seats.
        try {

            bookingService.confirmBooking(bookingId, showId, seatIds);


        } catch (RuntimeException confirmationFailure) {

            // Payment succeeded but confirmation failed: refund.
            try {
                walletClient.refund(walletUserId, total, bookingId);

                bookingService.releaseBookingLocks(
                        showId, seatIds, userId, bookingId
                );

            } catch (RuntimeException refundFailure) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Confirmation and refund require reconciliation. "
                                + "Booking ID: " + bookingId,
                        refundFailure
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat confirmation failed; refund was processed",
                    confirmationFailure
            );
        }

        return new BookingResponse(
                bookingId, showId, seatIds, total, "CONFIRMED"
        );
    }
    private Long parseUserId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID must be numeric",
                    ex
            );
        }
    }

    public record BookingResponse(
            String bookingId,
            Long showId,
            List<Long> seatIds,
            BigDecimal totalAmount,
            String status) {
    }
}