package com.example.booking_service.repository;

import com.example.booking_service.common.BookingSagaStatus;
import com.example.booking_service.entity.BookingSaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingSagaRepository
        extends JpaRepository<BookingSaga, Long> {

    Optional<BookingSaga> findByBookingId(String bookingId);
    List<BookingSaga> findTop100ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            BookingSagaStatus status,
            LocalDateTime cutoff
    );
}