package com.example.booking_service.controller;

import com.example.booking_service.dto.request.SeatLockRequest;
import com.example.booking_service.dto.response.SeatLockResponse;
import com.example.booking_service.dto.response.ShowSeatResponse;
import com.example.booking_service.service.BookingService;
import com.example.booking_service.service.BookingOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;
    private final BookingOrchestrator bookingOrchestrator;


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
    @PostMapping("/confirm/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmBooking(
            @PathVariable String bookingId,
            @RequestParam Long showId,
            @RequestBody List<Long> seatIds
    ) {
        bookingService.confirmBooking(bookingId, showId, seatIds);
    }

    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingOrchestrator.BookingResponse bookSeats(
            @Valid @RequestBody SeatLockRequest request) {

        return bookingOrchestrator.bookSeats(request);
    }
}