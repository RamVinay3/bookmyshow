package com.example.theatreservice.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SCREENS",
       uniqueConstraints = @UniqueConstraint(name = "UK_SCREEN_THEATRE_NAME", columnNames = {"THEATRE_ID", "NAME"}))
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "THEATRE_ID", nullable = false)
    private Theatre theatre;

    @Column(name = "NAME", nullable = false, length = 80)
    private String name;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rowLabel ASC, seatNumber ASC")
    private List<Seat> seats = new ArrayList<>();

    protected Screen() {}

    public Screen(String name) {
        this.name = name;
    }

    public void setTheatre(Theatre theatre) { this.theatre = theatre; }
    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.setScreen(this);
    }

    public Long getId() { return id; }
    public Theatre getTheatre() { return theatre; }
    public String getName() { return name; }
    public List<Seat> getSeats() { return seats; }
}
