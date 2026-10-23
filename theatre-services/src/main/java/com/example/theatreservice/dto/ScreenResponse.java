package com.example.theatreservice.dto;

import java.util.List;

public record ScreenResponse(
        Long id,
        Long theatreId,
        String name,
        int seatCount,
        List<SeatResponse> seats
) {}
