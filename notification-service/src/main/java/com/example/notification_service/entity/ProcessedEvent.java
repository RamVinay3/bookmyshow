package com.example.notification_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROCESSED_EVENTS")
public class ProcessedEvent {

    @Id
    @Column(name = "EVENT_ID", length = 36)
    private String eventId;

    @Column(name = "BOOKING_ID", nullable = false, length = 36)
    private String bookingId;

    @Column(name = "PROCESSED_AT", nullable = false)
    private LocalDateTime processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(String eventId, String bookingId) {
        this.eventId = eventId;
        this.bookingId = bookingId;
        this.processedAt = LocalDateTime.now();
    }
}