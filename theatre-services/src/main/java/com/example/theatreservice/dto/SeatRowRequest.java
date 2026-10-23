package com.example.theatreservice.dto;

import com.example.theatreservice.entity.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SeatRowRequest(
        @NotBlank @Size(max = 5) String rowLabel,
        @NotEmpty List<@NotNull Integer> seatNumbers,
        @NotNull SeatType seatType
) {}
