package com.example.booking_service.entity;


import com.example.booking_service.common.SeatType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;



@Getter
@Setter
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

    @Column(name = "BOOKING_ID",length = 36)
    private String bookingId;

    protected ShowSeat() {
    }

    public ShowSeat(Long showId, Long seatId,
                    String rowLabel, Integer seatNumber, SeatType seatType,
                    BigDecimal price,String bookingId) {
        this.showId = showId;
        this.seatId = seatId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.price = price;
        this.bookingId = bookingId;
    }



    public void lock(String userId,String bookingId, LocalDateTime expiry) {
        this.status = SeatStatus.LOCKED;
        this.lockedBy = userId;
        this.lockedUntil = expiry;
        this.bookingId=bookingId;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.lockedBy = null;
        this.lockedUntil = null;
        this.bookingId = null;
    }

    public void book() {
        this.status = SeatStatus.BOOKED;
        this.lockedBy = null;
        this.lockedUntil = null;
        this.bookingId = null;
    }
}