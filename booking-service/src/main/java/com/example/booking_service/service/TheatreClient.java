package com.example.booking_service.service;

import com.example.booking_service.dto.response.PhysicalSeat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class TheatreClient {

    private final WebClient client;

    public TheatreClient(
            WebClient.Builder builder,
            @Value("${services.theatre.base-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public List<PhysicalSeat> getSeats(Long screenId) {
        return client.get()
                .uri("/api/screens/{screenId}/seats", screenId)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<List<PhysicalSeat>>() {})
                .block();
    }
}