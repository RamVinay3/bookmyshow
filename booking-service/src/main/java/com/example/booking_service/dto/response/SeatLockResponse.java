package com.example.booking_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SeatLockResponse(
        String bookingId,
        Long showId,
        List<Long> seatIds,
        LocalDateTime expiresAt,
        String message

) {}