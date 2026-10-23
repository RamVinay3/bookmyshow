package com.example.theatreservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TheatreResponse(
        Long id,
        String name,
        String address,
        String city,
        LocalDateTime createdAt,
        List<ScreenResponse> screens
) {}
