package com.example.booking_service.exception;

public class MovieServiceUnReachableException extends  RuntimeException{

    public MovieServiceUnReachableException(String message){
        super(message);
    }
}
