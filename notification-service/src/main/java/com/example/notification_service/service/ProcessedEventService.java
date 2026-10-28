package com.example.notification_service.service;

import com.example.notification_service.events.BookingConfirmedEvent;
import com.example.notification_service.entity.ProcessedEvent;
import com.example.notification_service.respository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessedEventService {

    private final ProcessedEventRepository repository;

    public ProcessedEventService(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void markProcessed(BookingConfirmedEvent event) {
        repository.saveAndFlush(
                new ProcessedEvent(
                        event.eventId().toString(),
                        event.bookingId()
                )
        );
    }
}