package com.example.movie_service.Mappers;

import com.example.movie_service.dto.request.MovieRequestDTO;
import com.example.movie_service.dto.response.MovieResponseDTO;
import com.example.movie_service.entity.Movie;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class MovieMapper {

    public Movie requestToEntity( MovieRequestDTO request){


        return Movie.builder().
                title(request.getTitle()).
                description(request.getDescription()).
                language(request.getLanguage()).
                genres(request.getGenre()).
                cast(request.getCast()).
                build();
    }

    public MovieResponseDTO entityToResponse(Movie movie){
        return MovieResponseDTO.builder().
                title(movie.getTitle()).
                description(movie.getDescription()).
                language(movie.getLanguage()).
                cast(movie.getCast()).
                genre(movie.getGenres()).
                id(movie.getId()).
                build();
    }

    public List<MovieResponseDTO> entityToResponse(List<Movie> movies){
        List<MovieResponseDTO> movieResponses = new ArrayList<>();
        for(Movie movie: movies){
            movieResponses.add(this.entityToResponse(movie));
        }
        return movieResponses;

    }



}
