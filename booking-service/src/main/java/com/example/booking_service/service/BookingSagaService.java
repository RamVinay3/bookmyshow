package com.example.booking_service.service;

import com.example.booking_service.common.BookingSagaStatus;
import com.example.booking_service.entity.BookingSaga;
import com.example.booking_service.repository.BookingSagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BookingSagaService {

    private final BookingSagaRepository bookingSagaRepository;


    @Transactional
    public void createSaga(
            String bookingId,
            Long userId,
            Long showId
    ) {
        BookingSaga saga = new BookingSaga();

        saga.setBookingId(bookingId);
        saga.setUserId(userId);
        saga.setShowId(showId);
        saga.setStatus(BookingSagaStatus.SEATS_LOCKED);

        bookingSagaRepository.save(saga);
    }

    @Transactional
    public void updateSaga(
            String bookingId,
            BookingSagaStatus status,
            BigDecimal totalAmount
    ) {
        BookingSaga saga = bookingSagaRepository
                .findByBookingId(bookingId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Saga not found for booking: " + bookingId
                        )
                );

        saga.setStatus(status);

        if (totalAmount != null) {
            saga.setTotalAmount(totalAmount);
        }
    }
}