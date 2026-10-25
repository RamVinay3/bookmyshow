package com.example.showservice.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.example.showservice.common.SeatType;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "show_seat_prices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_show_seat_price",
                columnNames = {"show_id", "seat_type"}
        )
)
public class ShowSeatPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 30)
    private SeatType seatType;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}