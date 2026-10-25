package com.example.booking_service.repository;

import com.example.booking_service.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;

import java.util.List;

public interface ShowSeatRepository
        extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowIdOrderByRowLabelAscSeatNumberAsc(
            Long showId);
    long countByShowId(Long showId);

    boolean existsByShowIdAndSeatId(Long showId, Long seatId);


    @Modifying
    @Query("""
    update ShowSeat s
       set s.status = 'LOCKED',
           s.lockedBy = :userId,
           s.lockedUntil = :expiry
     where s.showId = :showId
       and s.seatId = :seatId
       and (
           s.status = 'AVAILABLE'
           or (s.status = 'LOCKED' and s.lockedUntil < :now)
       )
    """)
    int lockSeat(
            @Param("showId") Long showId,
            @Param("seatId") Long seatId,
            @Param("userId") String userId,
            @Param("expiry") LocalDateTime expiry,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
    update ShowSeat s
       set s.status = 'AVAILABLE',
           s.lockedBy = null,
           s.lockedUntil = null
     where s.showId = :showId
       and s.status = 'LOCKED'
       and s.lockedUntil < :now
    """)
    int releaseExpiredLocks(
            @Param("showId") Long showId,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
    update ShowSeat s
       set s.status = 'AVAILABLE',
           s.lockedBy = null,
           s.lockedUntil = null
     where s.showId = :showId
       and s.seatId in :seatIds
       and s.status = 'LOCKED'
       and s.lockedBy = :userId
    """)
    int releaseUserLocks(
            @Param("showId") Long showId,
            @Param("seatIds") Collection<Long> seatIds,
            @Param("userId") String userId
    );
}