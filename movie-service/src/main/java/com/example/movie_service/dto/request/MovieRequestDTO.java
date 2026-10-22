package com.example.movie_service.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MovieRequestDTO {
    private String title;
    private String language;
    private  String description;
    List<Cast> cast;
    List<String>genre;
}
