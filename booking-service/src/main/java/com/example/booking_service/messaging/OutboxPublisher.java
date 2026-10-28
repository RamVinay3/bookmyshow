package com.example.booking_service.messaging;

import com.example.booking_service.common.EventStatus;
import com.example.booking_service.entity.OutBoxEvents;
import com.example.booking_service.repository.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutBoxEventRepository outboxRepository;

    private final BookingEventProducer bookingEventProducer;

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {

        List<OutBoxEvents> events =
                outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(EventStatus.PENDING);

        for (OutBoxEvents event : events) {
            try {
                bookingEventProducer.publishBookingConfirmed(event.getPayload()).get();

                markAsPublished(event.getEventId());
                log.info("Outbox event published. eventId={}",
                        event.getEventId());

            } catch (Exception ex) {
                // Leave it PENDING so a later poll can retry.
                log.error("Failed to publish outbox event: {}",
                        event.getEventId(), ex);
            }
        }
    }

    @Transactional
    public void markAsPublished(String eventId) {
        OutBoxEvents event = outboxRepository.findById(eventId)
                .orElseThrow();

        event.setStatus(EventStatus.PUBLISHED);


        outboxRepository.save(event);
    }
}
