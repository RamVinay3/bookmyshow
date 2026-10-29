package com.example.booking_service.service;

import com.example.booking_service.common.BookingSagaStatus;
import com.example.booking_service.entity.BookingSaga;
import com.example.booking_service.entity.SeatStatus;
import com.example.booking_service.entity.ShowSeat;
import com.example.booking_service.repository.BookingSagaRepository;
import com.example.booking_service.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingSagaRecoveryService {

    private final BookingSagaRepository bookingSagaRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingSagaService bookingSagaService;
    private final BookingService bookingService;
    private final WalletClient walletClient;

    @Scheduled(fixedDelayString = "${booking.recovery.fixed-delay-ms:60000}")
    @Transactional
    public void recoverSagas() {

        LocalDateTime now = LocalDateTime.now();

        List<BookingSaga> sagas =
                bookingSagaRepository.lockRecoverableSagas(
                        List.of(
                                "SEATS_LOCKED",
                                "PAYMENT_PENDING",
                                "PAYMENT_OUTCOME_UNKNOWN",
                                "PAYMENT_DEBITED",
                                "CONFIRMATION_PENDING",
                                "REFUND_PENDING"
                        ),
                        now.minusMinutes(2)
                );

        for (BookingSaga saga : sagas) {
            try {
                recoverSaga(saga);
            } catch (Exception ex) {
                // Leave the current state unchanged for a later retry.
                log.error(
                        "Saga recovery failed. bookingId={}, status={}",
                        saga.getBookingId(),
                        saga.getStatus(),
                        ex
                );
            }
        }
    }

    private void recoverSaga(BookingSaga saga) {

        String bookingId = saga.getBookingId();

        List<ShowSeat> seats =
                showSeatRepository.findByBookingId(bookingId);

        switch (saga.getStatus()) {

            case SEATS_LOCKED -> {
                int released =
                        showSeatRepository.releaseExpiredLocksByBookingId(
                                bookingId,
                                LocalDateTime.now()
                        );

                if (released > 0 || seats.isEmpty()) {
                    saga.setStatus(BookingSagaStatus.EXPIRED);
                }
            }

            case PAYMENT_PENDING, PAYMENT_OUTCOME_UNKNOWN ->
                    reconcilePayment(saga, seats);

            case PAYMENT_DEBITED, CONFIRMATION_PENDING ->
                    confirmOrRefund(saga, seats);

            case REFUND_PENDING ->
                    refundAndRelease(saga, seats);

            default -> {
                // No recovery action required.
            }
        }
    }

    private void reconcilePayment(
            BookingSaga saga,
            List<ShowSeat> seats
    ) {
        String bookingId = saga.getBookingId();

        WalletClient.WalletTransactionLookupResponse result =
                walletClient.findBookingDebit(
                        saga.getUserId(),
                        bookingId
                );

        if (result.found()) {
            saga.setStatus(BookingSagaStatus.PAYMENT_DEBITED);
            confirmOrRefund(saga, seats);
            return;
        }

        // No debit exists: release the booking's remaining locks.
        releaseLocks(saga, seats);
        saga.setStatus(BookingSagaStatus.FAILED);
    }

    private void confirmOrRefund(
            BookingSaga saga,
            List<ShowSeat> seats
    ) {
        if (seats.isEmpty()) {
            saga.setStatus(BookingSagaStatus.REFUND_PENDING);
            refundAndRelease(saga, seats);
            return;
        }

        String bookingId = saga.getBookingId();

        List<Long> seatIds = seats.stream()
                .map(ShowSeat::getSeatId)
                .toList();

        BigDecimal total = saga.getTotalAmount();

        if (total == null) {
            total = seats.stream()
                    .map(ShowSeat::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            saga.setTotalAmount(total);
        }

        boolean alreadyBooked = seats.stream().allMatch(seat ->
                seat.getStatus() == SeatStatus.BOOKED
                        && bookingId.equals(seat.getBookingId())
        );

        if (!alreadyBooked) {
            try {
                bookingService.confirmBooking(
                        bookingId,
                        saga.getShowId(),
                        seatIds,
                        total,
                        String.valueOf(saga.getUserId())
                );
            } catch (RuntimeException ex) {
                saga.setStatus(BookingSagaStatus.REFUND_PENDING);
                refundAndRelease(saga, seats);
                return;
            }
        }

        saga.setStatus(BookingSagaStatus.CONFIRMED);
    }

    private void refundAndRelease(
            BookingSaga saga,
            List<ShowSeat> seats
    ) {
        walletClient.refund(
                saga.getUserId(),
                saga.getTotalAmount(),
                saga.getBookingId()
        );

        releaseLocks(saga, seats);
        saga.setStatus(BookingSagaStatus.REFUNDED);
    }

    private void releaseLocks(
            BookingSaga saga,
            List<ShowSeat> seats
    ) {
        if (seats.isEmpty()) {
            return;
        }

        showSeatRepository.releaseBookingLocks(
                saga.getShowId(),
                seats.stream().map(ShowSeat::getSeatId).toList(),
                String.valueOf(saga.getUserId()),
                saga.getBookingId()
        );
    }
}