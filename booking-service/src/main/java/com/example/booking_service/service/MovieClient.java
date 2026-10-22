package com.example.booking_service.service;

import com.example.booking_service.dto.response.MovieResponse;
import com.example.booking_service.exception.MovieNotFoundException;
import com.example.booking_service.exception.MovieServiceTimeoutException;
import java.util.concurrent.TimeoutException;

import com.example.booking_service.exception.MovieServiceUnReachableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class MovieClient {
    private final WebClient webClient;

    public MovieClient(WebClient.Builder builder,
                       @Value("${services.movie.base-url}") String baseUrl){
        webClient=builder.baseUrl(baseUrl).build();
    }
    @Retry(name = "movieService")
    @CircuitBreaker(name="movieService",
            fallbackMethod = "movieFallback")
    public MovieResponse getMovie(Long id){

        System.out.println("this is actual implementation"+id);
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

    private MovieResponse movieFallback(
            Long movieId,
            Throwable exception) {
         // [Spring Circuit Breaker proxy Box[ Retry proxy Box [ actual Error
        // if any error comes from the actual error and it was considered under retry it will execute
        //otherwise it will go to spring circuit Breaker fallback method and then send any of the below as error.
        if(exception instanceof MovieServiceTimeoutException ) throw new MovieServiceTimeoutException(exception.getMessage(),exception);
        if(exception instanceof MovieNotFoundException) throw new MovieNotFoundException(movieId);
        throw new MovieServiceUnReachableException("Movie Service unreachable from Booking Service");
    }
}
