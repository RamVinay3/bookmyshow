package com.example.booking_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SeatLockRequest(
        @NotNull Long showId,
        @NotBlank String userId,
        @NotEmpty
        @Size(max = 10)
        List<@NotNull Long> seatIds
) {}