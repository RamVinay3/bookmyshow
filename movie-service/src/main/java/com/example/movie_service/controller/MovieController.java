package com.example.movie_service.controller;


import com.example.movie_service.dto.request.MovieRequestDTO;
import com.example.movie_service.dto.response.MovieResponseDTO;
import com.example.movie_service.services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody MovieRequestDTO movie) {
        return movieService.create(movie);
    }

    @GetMapping
    public List<MovieResponseDTO> findAll() {
        return movieService.findAll();
    }

    @GetMapping("/{id}")
    public MovieResponseDTO findById(@PathVariable Long id) throws  InterruptedException {
        return movieService.findById(id);
    }

}
