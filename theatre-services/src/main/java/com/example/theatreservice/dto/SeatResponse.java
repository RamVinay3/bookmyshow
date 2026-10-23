package com.example.theatreservice.dto;

import com.example.theatreservice.entity.SeatType;

public record SeatResponse(
        Long id,
        Long screenId,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType
) {}
