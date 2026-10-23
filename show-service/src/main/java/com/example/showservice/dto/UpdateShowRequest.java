package com.example.showservice.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record UpdateShowRequest(@NotNull @Future LocalDateTime startTime, @NotNull @Future LocalDateTime endTime,
                                @NotBlank String language, @NotBlank String format) {
}
