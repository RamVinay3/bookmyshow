package com.example.notification_service.messaging;

import com.example.notification_service.events.BookingConfirmedEvent;
import com.example.notification_service.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);
    private final ProcessedEventService processedEventService;


    public BookingEventConsumer(
            ProcessedEventService processedEventService) {
        this.processedEventService = processedEventService;
    }

    @KafkaListener(
            topics = "booking-confirmed",
            groupId = "notification-service"
    )
    public void consumeBookingConfirmed(BookingConfirmedEvent event) {
        try {
            processedEventService.markProcessed(event);

            log.info(
                    "Booking confirmation processed. bookingId={}, userId={}, showId={}, seats={}, amount={}",
                    event.bookingId(),
                    event.userId(),
                    event.showId(),
                    event.seatIds(),
                    event.totalAmount()
            );

        } catch (Exception ex) {
            log.info(
                    "Duplicate booking event skipped. eventId={}, bookingId={}",
                    event.eventId(),
                    event.bookingId()
            );
        }
    }
}
