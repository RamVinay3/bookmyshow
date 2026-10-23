package com.example.theatreservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateScreenRequest(
        @NotBlank @Size(max = 80) String name
) {}
