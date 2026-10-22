package com.example.movie_service.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> movieNotFound(MovieNotFoundException exception,
                                                          HttpServletRequest request){
        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );


    }

    public ResponseEntity<ErrorResponseDTO> buildError(HttpStatus status,
                                                       String message,
                                                       String path){
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                message,
                path
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
