package com.example.booking_service.repository;

import com.example.booking_service.common.BookingSagaStatus;
import com.example.booking_service.entity.BookingSaga;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingSagaRepository
        extends JpaRepository<BookingSaga, Long> {

    Optional<BookingSaga> findByBookingId(String bookingId);
    List<BookingSaga> findTop100ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            BookingSagaStatus status,
            LocalDateTime cutoff
    );

    
     @Query(value = """
                   SELECT *
                            FROM BOOKING_SAGA
                            WHERE ID IN (
                                SELECT ID
                                FROM (
                                    SELECT ID
                                    FROM BOOKING_SAGA
                                    WHERE STATUS IN (:statuses)
                                      AND UPDATED_AT < :cutoff
                                    ORDER BY UPDATED_AT
                                )
                                WHERE ROWNUM <= 100
                            )
                            FOR UPDATE SKIP LOCKED
            
            """, nativeQuery = true)
        List<BookingSaga> lockRecoverableSagas(
                @Param("statuses") List<String> statuses,
                @Param("cutoff") LocalDateTime cutoff
        );
    }
