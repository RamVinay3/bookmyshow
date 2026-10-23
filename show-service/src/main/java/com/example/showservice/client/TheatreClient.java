package com.example.showservice.client;

import com.example.showservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;

@Component
public class TheatreClient {
    private final WebClient client;

    public TheatreClient(WebClient.Builder builder, @Value("${services.theatre.base-url:http://theatre-service}") String url) {
        client = builder.baseUrl(url).build();
    }

    public void assertTheatreExists(Long id) {
        try {
            client.get().uri("/api/theatres/{id}", id).retrieve().toBodilessEntity().block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Theatre " + id + " was not found.");
        }
    }

    public void assertScreenBelongsToTheatre(Long theatreId, Long screenId) {
        try {
            ScreenResponse screen = client.get().uri("/api/screens/{id}", screenId).retrieve().bodyToMono(ScreenResponse.class).block();
            if (screen == null || screen.theatreId() == null || !screen.theatreId().equals(theatreId))
                throw new ResourceNotFoundException("Screen " + screenId + " does not belong to theatre " + theatreId + ".");
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Screen " + screenId + " was not found.");
        }
    }

    private record ScreenResponse(Long id, Long theatreId, String name, Integer seatCount) {
    }
}
