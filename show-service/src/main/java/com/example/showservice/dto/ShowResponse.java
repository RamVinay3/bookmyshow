package com.example.showservice.dto;

import com.example.showservice.entity.ShowStatus;

import java.time.LocalDateTime;

public record ShowResponse(Long id, Long movieId, Long theatreId, Long screenId, LocalDateTime startTime,
                           LocalDateTime endTime, String language, String format, ShowStatus status,
                           LocalDateTime createdAt) {
}
