package com.example.showservice.controller;

import com.example.showservice.dto.*;
import com.example.showservice.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {
    private final ShowService service;

    public ShowController(ShowService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowResponse create(@Valid @RequestBody CreateShowRequest request) {
        return service.create(request);
    }

    @GetMapping("/{showId}")
    public ShowResponse get(@PathVariable Long showId) {
        return service.get(showId);
    }

    @GetMapping
    public List<ShowResponse> search(@RequestParam(required = false) Long movieId, @RequestParam(required = false) Long theatreId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.search(movieId, theatreId, date);
    }

    @PutMapping("/{showId}")
    public ShowResponse update(@PathVariable Long showId, @Valid @RequestBody UpdateShowRequest request) {
        return service.update(showId, request);
    }

    @DeleteMapping("/{showId}")
    public ShowResponse cancel(@PathVariable Long showId) {
        return service.cancel(showId);
    }
}
