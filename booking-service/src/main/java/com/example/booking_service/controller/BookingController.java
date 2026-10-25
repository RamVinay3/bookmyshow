package com.example.booking_service.controller;

import com.example.booking_service.dto.request.SeatLockRequest;
import com.example.booking_service.dto.response.SeatLockResponse;
import com.example.booking_service.dto.response.ShowSeatResponse;
import com.example.booking_service.service.BookingService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/shows/{showId}/seats")
    public List<ShowSeatResponse> getSeats(
            @PathVariable Long showId) {
        return bookingService.getSeats(showId);
    }

    @PostMapping("/seat-locks")
    @ResponseStatus(HttpStatus.CREATED)
    public SeatLockResponse lockSeats(
            @Valid @RequestBody SeatLockRequest request) {
        return bookingService.lockSeats(request);
    }

    @DeleteMapping("/seat-locks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseSeats(
            @Valid @RequestBody SeatLockRequest request) {
        bookingService.releaseSeats(request);
    }
}