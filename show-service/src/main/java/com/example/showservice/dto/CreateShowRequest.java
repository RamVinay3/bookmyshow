package com.example.showservice.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateShowRequest(@NotNull @Positive Long movieId, @NotNull @Positive Long theatreId,
                                @NotNull @Positive Long screenId,
                                @NotNull @Future LocalDateTime startTime, @NotNull @Future LocalDateTime endTime,
                                @NotBlank String language, @NotBlank String format) {
}
