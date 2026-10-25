package com.example.booking_service.dto.response;

import java.math.BigDecimal;

public record SeatTypePrice(
        Long id,
        Long showId,
        String seatType,
        BigDecimal price
) {
}