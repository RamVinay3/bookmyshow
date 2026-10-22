package com.example.booking_service.exception;

public class MovieServiceTimeoutException extends RuntimeException {

    public MovieServiceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}