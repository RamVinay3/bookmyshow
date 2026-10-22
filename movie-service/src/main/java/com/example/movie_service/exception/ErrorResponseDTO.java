package com.example.movie_service.exception;


import java.time.LocalDateTime;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String message,
        String path

) {
};