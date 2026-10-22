package com.example.booking_service.config;

import com.example.booking_service.dto.response.MovieResponse;
import com.example.booking_service.service.MovieClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class MovieClientTest {

    private final MovieClient movieClient;

//    @Bean
//    CommandLineRunner testMovieClient(MovieClient movieClient) {
//        return args -> {
//            MovieResponse movie =
//                    movieClient.getMovie(3L);
//
//            System.out.println(movie);
//        };
//    }


}
