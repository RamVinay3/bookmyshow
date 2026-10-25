package com.example.booking_service.entity;


import com.example.booking_service.common.SeatType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;




@Entity
@Table(
        name = "SHOW_SEATS",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_SHOW_SEAT",
                columnNames = {"SHOW_ID", "SEAT_ID"}
        )
)
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SHOW_ID", nullable = false)
    private Long showId;

    @Column(name = "SEAT_ID", nullable = false)
    private Long seatId;

    @Column(name = "ROW_LABEL", nullable = false)
    private String rowLabel;

    @Column(name = "SEAT_NUMBER", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "LOCKED_BY")
    private String lockedBy;

    @Column(name = "LOCKED_UNTIL")
    private LocalDateTime lockedUntil;

    @Column(name = "SEAT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected ShowSeat() {
    }

    public ShowSeat(Long showId, Long seatId,
                    String rowLabel, Integer seatNumber) {
        this.showId = showId;
        this.seatId = seatId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
    }

    public Long getId() {
        return id;
    }

    public Long getShowId() {
        return showId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void lock(String userId, LocalDateTime expiry) {
        this.status = SeatStatus.LOCKED;
        this.lockedBy = userId;
        this.lockedUntil = expiry;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.lockedBy = null;
        this.lockedUntil = null;
    }

    public void book() {
        this.status = SeatStatus.BOOKED;
        this.lockedBy = null;
        this.lockedUntil = null;
    }
}