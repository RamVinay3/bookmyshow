package com.example.showservice.controller;

import com.example.showservice.dto.SeatTypePriceRequest;
import com.example.showservice.entity.ShowSeatPrice;
import com.example.showservice.service.ShowSeatPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shows/{showId}/prices")
public class ShowSeatPriceController {

    private final ShowSeatPriceService priceService;

    @PutMapping
    public List<ShowSeatPrice> configurePrices(
            @PathVariable Long showId,
            @Valid @RequestBody List<@Valid SeatTypePriceRequest> requests
    ) {
        return priceService.configurePrices(showId, requests);
    }

    @GetMapping
    public List<ShowSeatPrice> getPrices(
            @PathVariable Long showId
    ) {
        return priceService.getPrices(showId);
    }
}