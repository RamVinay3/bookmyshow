package com.example.booking_service.dto.response;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class MovieResponse {
    private String title;
    private String language;
    private  String description;

    List<String>genre;
    private Long id;
    List<Cast> cast;

}
