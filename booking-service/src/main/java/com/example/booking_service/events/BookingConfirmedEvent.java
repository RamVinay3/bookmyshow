package com.example.booking_service.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BookingConfirmedEvent(
        UUID eventId,
        String bookingId,
        String userId,
        Long showId,
        List<Long> seatIds,
        BigDecimal totalAmount,
        LocalDateTime occurredAt
) {
}