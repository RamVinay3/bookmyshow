package com.example.theatreservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "THEATRES")
public class Theatre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 120)
    private String name;

    @Column(name = "ADDRESS", nullable = false, length = 300)
    private String address;

    @Column(name = "CITY", nullable = false, length = 100)
    private String city;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Screen> screens = new ArrayList<>();

    protected Theatre() {}

    public Theatre(String name, String address, String city) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.createdAt = LocalDateTime.now();
    }

    public void updateDetails(String name, String address, String city) {
        this.name = name;
        this.address = address;
        this.city = city;
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
        screen.setTheatre(this);
    }

    public void removeScreen(Screen screen) {
        screens.remove(screen);
        screen.setTheatre(null);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Screen> getScreens() { return screens; }
}
