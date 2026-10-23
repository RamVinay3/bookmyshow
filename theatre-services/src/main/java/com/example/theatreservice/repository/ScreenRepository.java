package com.example.theatreservice.repository;

import com.example.theatreservice.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByTheatreIdOrderByIdAsc(Long theatreId);
    boolean existsByTheatreIdAndNameIgnoreCase(Long theatreId, String name);
}
