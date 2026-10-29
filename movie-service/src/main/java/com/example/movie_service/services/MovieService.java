package com.example.movie_service.services;


import com.example.movie_service.Mappers.MovieMapper;
import com.example.movie_service.dto.request.MovieRequestDTO;
import com.example.movie_service.dto.response.MovieResponseDTO;
import com.example.movie_service.entity.Movie;
import com.example.movie_service.exception.MovieNotFoundException;
import com.example.movie_service.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private  final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Value("${server.port}")
    private  int  port;

    public String create(MovieRequestDTO requestDTO){

        Movie movie= movieMapper.requestToEntity(requestDTO);

        movieRepository.save(movie);

        return "The movie is successfully created";
    }

    public List<MovieResponseDTO> findAll() {
        List<Movie> movies= movieRepository.findAll();

        return movieMapper.entityToResponse(movies);
    }

    @Cacheable(value = "movieDetails", key = "#id")
    public MovieResponseDTO findById(Long id) throws InterruptedException {
        Movie movie= movieRepository.findById(id)
                .orElseThrow(() ->
                        new MovieNotFoundException(id));
//        Thread.sleep(4000);
        log.info("port:{}",port);
        return movieMapper.entityToResponse(movie);
    }


}
