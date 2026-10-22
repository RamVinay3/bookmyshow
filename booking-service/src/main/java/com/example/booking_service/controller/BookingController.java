package com.example.booking_service.controller;


import com.example.booking_service.dto.request.BookingRequestDTO;
import com.example.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String book(@RequestBody BookingRequestDTO requestDTO){

        return bookingService.book(requestDTO);

    }

}
