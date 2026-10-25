package com.example.showservice.dto;

import com.example.showservice.common.SeatType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SeatTypePriceRequest(
        @NotNull SeatType seatType,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal price
) {
}