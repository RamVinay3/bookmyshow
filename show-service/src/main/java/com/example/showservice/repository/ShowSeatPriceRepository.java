package com.example.showservice.repository;

import com.example.showservice.common.SeatType;
import com.example.showservice.entity.ShowSeatPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShowSeatPriceRepository
        extends JpaRepository<ShowSeatPrice, Long> {

    List<ShowSeatPrice> findByShowId(Long showId);

    Optional<ShowSeatPrice> findByShowIdAndSeatType(
            Long showId,
            SeatType seatType
    );
}