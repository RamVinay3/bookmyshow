package com.example.booking_service.exception;


import io.netty.handler.timeout.ReadTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import  java.util.concurrent.TimeoutException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exception(Exception ex, HttpServletRequest request){

        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(ex.getMessage());
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<String> MovieNotFound(MovieNotFoundException ex,HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<Map<String, String>> handleConnectionFailure(
            WebClientRequestException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Movie Service is unavailable"));
    }


    @ExceptionHandler(MovieServiceTimeoutException.class)
    public ResponseEntity<Map<String, String>> handleMovieServiceTimeout(
            MovieServiceTimeoutException ex) {

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(Map.of("error", ex.getMessage()));
    }

}
