package com.example.movie_service.dto.response;

import com.example.movie_service.dto.request.Cast;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MovieResponseDTO {

    private String title;
    private String language;
    private  String description;
    List<Cast> cast;
    List<String>genre;
    private Long id;
}
