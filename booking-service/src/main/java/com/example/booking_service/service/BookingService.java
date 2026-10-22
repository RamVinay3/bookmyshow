package com.example.booking_service.service;

import com.example.booking_service.dto.request.BookingRequestDTO;
import com.example.booking_service.dto.response.MovieResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class BookingService {

    private final MovieClient movieClient;

    public String book(BookingRequestDTO requestDTO){

        MovieResponse response = movieClient.getMovie(requestDTO.getMovieId());
      log.info("response: {}",response);
        if(response==null){
            return "Movie is not available";
        }
        return "Booking successfully";
    }


}
