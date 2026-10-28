package com.example.booking_service.entity;


import com.example.booking_service.common.EventStatus;
import com.example.booking_service.common.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(
        name = "OUTBOX_EVENTS"
)
public class OutBoxEvents {

    @Id
    private String eventId;
    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private String payload;

    @Enumerated(EnumType.STRING)
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    public OutBoxEvents(String eventId, String aggregateId,EventType eventType,EventStatus status,String payload){
        this.eventId=eventId;
        this.aggregateId=aggregateId;
        this.eventType=eventType;
        this.status=status;
        this.payload=payload;
    }

    public OutBoxEvents(){}




    @PrePersist
    protected  void onCreate(){

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;


    }

    @PreUpdate
    protected void onUpdate(){
        publishedAt = LocalDateTime.now();
    }


}
