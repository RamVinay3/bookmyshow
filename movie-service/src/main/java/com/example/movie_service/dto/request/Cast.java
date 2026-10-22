package com.example.movie_service.dto.request;

import jakarta.persistence.Embeddable;

@Embeddable
public record Cast(String name,String role) {

};
