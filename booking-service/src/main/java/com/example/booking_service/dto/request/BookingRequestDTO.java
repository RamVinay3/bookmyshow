package com.example.booking_service.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BookingRequestDTO {

    private String userId;
    private  Long movieId;

}
