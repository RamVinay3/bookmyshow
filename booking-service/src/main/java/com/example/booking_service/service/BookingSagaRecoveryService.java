package com.example.booking_service.service;

import com.example.booking_service.common.BookingSagaStatus;
import com.example.booking_service.entity.BookingSaga;
import com.example.booking_service.repository.BookingSagaRepository;
import com.example.booking_service.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingSagaRecoveryService {

    private final BookingSagaRepository bookingSagaRepository;
    private final ShowSeatRepository showSeatRepository;

    @Scheduled(fixedDelayString = "${booking.recovery.fixed-delay-ms:60000}")
    @Transactional
    public void recoverExpiredSeatLocks() {

        LocalDateTime now = LocalDateTime.now();

        // Seat locks last five minutes in BookingService.
        LocalDateTime cutoff = now.minusMinutes(5);

        List<BookingSaga> expiredSagas =
                bookingSagaRepository
                        .findTop100ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                                BookingSagaStatus.SEATS_LOCKED,
                                cutoff
                        );

        for (BookingSaga saga : expiredSagas) {

            // Release only expired seats belonging to this booking.
            int releasedSeats =
                    showSeatRepository.releaseExpiredLocksByBookingId(
                            saga.getBookingId(),
                            now
                    );

            saga.setStatus(BookingSagaStatus.EXPIRED);

            log.info(
                    "Expired booking Saga. bookingId={}, releasedSeats={}",
                    saga.getBookingId(),
                    releasedSeats
            );
        }

        if (!expiredSagas.isEmpty()) {
            bookingSagaRepository.saveAll(expiredSagas);
        }
    }
}