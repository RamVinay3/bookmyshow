package com.example.theatreservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SEATS",
       uniqueConstraints = @UniqueConstraint(name = "UK_SEAT_SCREEN_ROW_NO",
               columnNames = {"SCREEN_ID", "ROW_LABEL", "SEAT_NUMBER"}))
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SCREEN_ID", nullable = false)
    private Screen screen;

    @Column(name = "ROW_LABEL", nullable = false, length = 5)
    private String rowLabel;

    @Column(name = "SEAT_NUMBER", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEAT_TYPE", nullable = false, length = 20)
    private SeatType seatType;

    protected Seat() {}

    public Seat(String rowLabel, Integer seatNumber, SeatType seatType) {
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public void setScreen(Screen screen) { this.screen = screen; }
    public Long getId() { return id; }
    public Long getScreenId() { return screen == null ? null : screen.getId(); }
    public String getRowLabel() { return rowLabel; }
    public Integer getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }
}
