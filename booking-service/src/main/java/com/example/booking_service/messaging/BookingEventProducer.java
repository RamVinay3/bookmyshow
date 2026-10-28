package com.example.booking_service.messaging;

import com.example.booking_service.events.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventProducer {

    private static final String TOPIC = "booking-confirmed";

    private final KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;

    public void publishBookingConfirmed(
            String bookingId,
            String userId,
            Long showId,
            List<Long> seatIds,
            BigDecimal totalAmount
    ) {
        BookingConfirmedEvent event = new BookingConfirmedEvent(
                UUID.randomUUID(),
                bookingId,
                userId,
                showId,
                List.copyOf(seatIds),
                totalAmount,
                LocalDateTime.now()
        );

        try {
            kafkaTemplate.send(TOPIC, bookingId, event)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error(
                                    "Failed to publish booking event. bookingId={}",
                                    bookingId,
                                    exception
                            );
                        } else {
                            log.info(
                                    "Published booking event. bookingId={}, partition={}, offset={}",
                                    bookingId,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                    });
        } catch (RuntimeException exception) {
            // Do not turn an already-confirmed booking into an API failure.
            log.error(
                    "Could not initiate publishing booking event. bookingId={}",
                    bookingId,
                    exception
            );
        }
    }
}