package com.example.booking_service.dto.response;

public record ShowDetails(
        Long id,
        Long movieId,
        Long theatreId,
        Long screenId,
        String startTime,
        String endTime,
        String language,
        String format,
        String status
) {}