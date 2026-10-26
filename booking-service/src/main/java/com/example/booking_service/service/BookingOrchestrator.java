package com.example.booking_service.service;

import com.example.booking_service.dto.request.SeatLockRequest;
import com.example.booking_service.dto.response.SeatLockResponse;
import com.example.booking_service.dto.response.ShowSeatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingOrchestrator {

    private final BookingService bookingService;
    private final WalletClient walletClient;

    public BookingOrchestrator(
            BookingService bookingService,
            WalletClient walletClient) {
        this.bookingService = bookingService;
        this.walletClient = walletClient;
    }

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