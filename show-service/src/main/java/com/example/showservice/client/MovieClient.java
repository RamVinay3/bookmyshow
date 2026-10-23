package com.example.showservice.client;

import com.example.showservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;

@Component
public class MovieClient {
    private final WebClient client;

    public MovieClient(WebClient.Builder builder, @Value("${services.movie.base-url:http://movie-service}") String url) {
        client = builder.baseUrl(url).build();
    }

    public void assertMovieExists(Long id) {
        try {
            client.get().uri("/api/movies/{id}", id).retrieve().toBodilessEntity().block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Movie " + id + " was not found.");
        }
    }
}
