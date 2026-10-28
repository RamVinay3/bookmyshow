package com.example.booking_service.service;

import com.example.booking_service.common.BookingSagaStatus;
import com.example.booking_service.dto.request.SeatLockRequest;
import com.example.booking_service.dto.response.SeatLockResponse;
import com.example.booking_service.dto.response.ShowSeatResponse;
import com.example.booking_service.entity.SeatStatus;
import com.example.booking_service.entity.ShowSeat;
import com.example.booking_service.messaging.BookingEventProducer;
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
    private final BookingSagaService bookingSagaService;


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

        bookingSagaService.updateSaga(
                bookingId,
                BookingSagaStatus.PAYMENT_PENDING,
                total
        );
        // 5. Debit the wallet.
        try {
            walletClient.debit(walletUserId, total, bookingId);
        } catch (WebClientResponseException ex) {

            if (ex.getStatusCode().is4xxClientError()) {
                bookingService.releaseBookingLocks(
                        showId, seatIds, userId, bookingId
                );
                bookingSagaService.updateSaga(
                        bookingId,
                        BookingSagaStatus.FAILED,
                        total
                );
                throw new ResponseStatusException(
                        HttpStatus.PAYMENT_REQUIRED,
                        "Wallet rejected the payment",
                        ex
                );
            }
            bookingSagaService.updateSaga(
                    bookingId,
                    BookingSagaStatus.PAYMENT_OUTCOME_UNKNOWN,
                    total
            );
            // The debit outcome may be unknown.
            // Keep the lock and reconcile using the same booking ID.
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment outcome is unknown; reconcile before retrying",
                    ex
            );
        } catch (RuntimeException ex) {

            bookingSagaService.updateSaga(
                    bookingId,
                    BookingSagaStatus.PAYMENT_OUTCOME_UNKNOWN,
                    total
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment outcome is unknown; reconcile before retrying",
                    ex
            );
        }
        bookingSagaService.updateSaga(
                bookingId,
                BookingSagaStatus.PAYMENT_DEBITED,
                total
        );

        bookingSagaService.updateSaga(
                bookingId,
                BookingSagaStatus.CONFIRMATION_PENDING,
                total
        );
        // 6. Confirm the seats.
        try {

            bookingService.confirmBooking(bookingId, showId, seatIds,total,userId);


        } catch (RuntimeException confirmationFailure) {


            bookingSagaService.updateSaga(
                    bookingId,
                    BookingSagaStatus.REFUND_PENDING,
                    total
            );
            // Payment succeeded but confirmation failed: refund.
            try {
                walletClient.refund(walletUserId, total, bookingId);

                bookingService.releaseBookingLocks(
                        showId, seatIds, userId, bookingId
                );

            } catch (RuntimeException refundFailure) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Confirmation and refu  nd require reconciliation. "
                                + "Booking ID: " + bookingId,
                        refundFailure
                );
            }
            bookingSagaService.updateSaga(
                    bookingId,
                    BookingSagaStatus.REFUNDED,
                    total
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat confirmation failed; refund was processed",
                    confirmationFailure
            );
        }


        bookingSagaService.updateSaga(
                bookingId,
                BookingSagaStatus.CONFIRMED,
                total
        );

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