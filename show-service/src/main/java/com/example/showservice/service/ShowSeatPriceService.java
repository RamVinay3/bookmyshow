package com.example.showservice.service;

import com.example.showservice.common.SeatType;
import com.example.showservice.dto.SeatTypePriceRequest;
import com.example.showservice.entity.ShowSeatPrice;
import com.example.showservice.repository.ShowSeatPriceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowSeatPriceService {

    private final ShowSeatPriceRepository repository;

    @Transactional
    public List<ShowSeatPrice> configurePrices(
            Long showId,
            List<SeatTypePriceRequest> requests
    ) {
        if (showId == null || showId <= 0) {
            throw new IllegalArgumentException("Invalid show ID");
        }

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one seat price is required"
            );
        }

        Map<SeatType, SeatTypePriceRequest> pricesByType =
                requests.stream().collect(Collectors.toMap(
                        SeatTypePriceRequest::seatType,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalArgumentException(
                                    "Duplicate seat type: " + first.seatType()
                            );
                        }
                ));

        return pricesByType.values().stream()
                .map(request -> {
                    ShowSeatPrice price = repository
                            .findByShowIdAndSeatType(
                                    showId,
                                    request.seatType()
                            )
                            .orElseGet(ShowSeatPrice::new);

                    price.setShowId(showId);
                    price.setSeatType(request.seatType());
                    price.setPrice(request.price());

                    return repository.save(price);
                })
                .toList();
    }

    public List<ShowSeatPrice> getPrices(Long showId) {
        return repository.findByShowId(showId);
    }
}