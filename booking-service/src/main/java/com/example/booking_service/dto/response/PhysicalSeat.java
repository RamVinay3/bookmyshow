package com.example.booking_service.dto.response;

public record PhysicalSeat(
        Long id,
        Long screenId,
        String rowLabel,
        Integer seatNumber,
        String seatType
) {}