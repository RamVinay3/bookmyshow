package com.example.showservice.dto;

import com.example.showservice.common.SeatType;
import java.math.BigDecimal;

public record SeatPriceResponse(
        Long id,
        Long showId,
        SeatType seatType,
        BigDecimal price
) {}