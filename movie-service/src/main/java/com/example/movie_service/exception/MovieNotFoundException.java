package com.example.movie_service.exception;

public class MovieNotFoundException  extends  RuntimeException{

    public MovieNotFoundException(Long id){
        super("The movie "+id+" is not available");
    }
}
