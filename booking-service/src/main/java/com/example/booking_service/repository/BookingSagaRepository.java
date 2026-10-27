package com.example.booking_service.repository;

import com.example.booking_service.entity.BookingSaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingSagaRepository
        extends JpaRepository<BookingSaga, Long> {

    Optional<BookingSaga> findByBookingId(String bookingId);
}