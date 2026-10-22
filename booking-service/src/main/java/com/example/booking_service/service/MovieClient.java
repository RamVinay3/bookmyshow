package com.example.booking_service.service;

import com.example.booking_service.dto.response.MovieResponse;
import com.example.booking_service.exception.MovieNotFoundException;
import com.example.booking_service.exception.MovieServiceTimeoutException;
import java.util.concurrent.TimeoutException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class MovieClient {
    private final WebClient webClient;

    public MovieClient(WebClient.Builder builder, @Value("${services.movie.base-url}") String baseUrl){
        webClient=builder.baseUrl(baseUrl).build();
    }

    public MovieResponse getMovie(Long id){
       return this.webClient.get().
               uri("/api/movies/{id}",id)
                .retrieve().
               // HTTP response errors
               onStatus(status -> status.value()==404,
                       response-> Mono.error(new MovieNotFoundException(id))).
               // Reactive/network exceptions
               bodyToMono(MovieResponse.class).
               timeout(Duration.ofSeconds(3)).
               onErrorMap(
                TimeoutException.class,
                ex -> new MovieServiceTimeoutException(
                        "Movie Service did not respond within 3 seconds",
                        ex)).
               block();
    }

}
