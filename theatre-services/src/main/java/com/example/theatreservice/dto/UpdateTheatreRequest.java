package com.example.theatreservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTheatreRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 300) String address,
        @NotBlank @Size(max = 100) String city
) {}
