package com.example.booking_service.entity;

import com.example.booking_service.common.BookingSagaStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "BOOKING_SAGA",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_BOOKING_SAGA_BOOKING_ID",
                columnNames = "BOOKING_ID"
        )
)
public class BookingSaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "BOOKING_ID", nullable = false, unique = true, length = 36)
    private String bookingId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "SHOW_ID", nullable = false)
    private Long showId;

    @Column(name = "TOTAL_AMOUNT", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 40)
    private BookingSagaStatus status;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}