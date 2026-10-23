package com.example.theatreservice.repository;

import com.example.theatreservice.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreen_IdOrderByRowLabelAscSeatNumberAsc(
            Long screenId
    );

    boolean existsByScreen_IdAndRowLabelIgnoreCaseAndSeatNumber(
            Long screenId,
            String rowLabel,
            Integer seatNumber
    );
}
