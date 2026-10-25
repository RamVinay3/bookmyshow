package com.example.booking_service.service;

import com.example.booking_service.dto.response.ShowDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ShowClient {

    private final WebClient client;

    public ShowClient(
            WebClient.Builder builder,
            @Value("${services.show.base-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public ShowDetails getShow(Long showId) {
        return client.get()
                .uri("/api/shows/{id}", showId)
                .retrieve()
                .bodyToMono(ShowDetails.class)
                .block();
    }
}