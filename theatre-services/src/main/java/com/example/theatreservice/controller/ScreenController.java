package com.example.theatreservice.controller;

import com.example.theatreservice.dto.ScreenResponse;
import com.example.theatreservice.dto.SeatResponse;
import com.example.theatreservice.dto.SeatRowRequest;
import com.example.theatreservice.service.TheatreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/screens")
public class ScreenController {
    private final TheatreService theatreService;

    public ScreenController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @GetMapping("/{screenId}")
    public ScreenResponse getScreen(@PathVariable Long screenId) {
        return theatreService.getScreen(screenId);
    }

    @PostMapping("/{screenId}/seats/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SeatResponse> createSeats(@PathVariable Long screenId,
                                          @RequestBody @NotEmpty List<@Valid SeatRowRequest> rows) {
        return theatreService.createSeats(screenId, rows);
    }

    @GetMapping("/{screenId}/seats")
    public List<SeatResponse> getSeats(@PathVariable Long screenId) {
        return theatreService.getSeats(screenId);
    }
}
