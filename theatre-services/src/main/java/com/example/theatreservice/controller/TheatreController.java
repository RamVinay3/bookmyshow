package com.example.theatreservice.controller;

import com.example.theatreservice.dto.*;
import com.example.theatreservice.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {
    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TheatreResponse createTheatre(@Valid @RequestBody CreateTheatreRequest request) {
        return theatreService.createTheatre(request);
    }

    @GetMapping
    public List<TheatreResponse> getTheatres(@RequestParam(required = false) String city) {
        return theatreService.getTheatres(city);
    }

    @GetMapping("/{theatreId}")
    public TheatreResponse getTheatre(@PathVariable Long theatreId) {
        return theatreService.getTheatre(theatreId);
    }

    @PutMapping("/{theatreId}")
    public TheatreResponse updateTheatre(@PathVariable Long theatreId,
                                         @Valid @RequestBody UpdateTheatreRequest request) {
        return theatreService.updateTheatre(theatreId, request);
    }

    @DeleteMapping("/{theatreId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheatre(@PathVariable Long theatreId) {
        theatreService.deleteTheatre(theatreId);
    }

    @PostMapping("/{theatreId}/screens")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreenResponse createScreen(@PathVariable Long theatreId,
                                       @Valid @RequestBody CreateScreenRequest request) {
        return theatreService.createScreen(theatreId, request);
    }

    @GetMapping("/{theatreId}/screens")
    public List<ScreenResponse> getScreens(@PathVariable Long theatreId) {
        return theatreService.getScreens(theatreId);
    }
}
