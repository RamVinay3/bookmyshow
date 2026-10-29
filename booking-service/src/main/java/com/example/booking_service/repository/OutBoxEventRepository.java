package com.example.booking_service.repository;

import com.example.booking_service.common.EventStatus;
import com.example.booking_service.entity.OutBoxEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutBoxEventRepository  extends JpaRepository<OutBoxEvents,String> {

    List<OutBoxEvents> findTop100ByStatusOrderByCreatedAtAsc(EventStatus status);

    @Query(value = """
                  SELECT *
                 FROM OUTBOX_EVENTS
                 WHERE EVENT_ID IN (
                     SELECT EVENT_ID
                     FROM (
                         SELECT EVENT_ID
                         FROM OUTBOX_EVENTS
                         WHERE STATUS = 'PENDING'
                         ORDER BY CREATED_AT
                     )
                     WHERE ROWNUM <= 25
                 )
                 FOR UPDATE SKIP LOCKED;
                """, nativeQuery = true)
    List<OutBoxEvents> lockPendingEvents();
}
