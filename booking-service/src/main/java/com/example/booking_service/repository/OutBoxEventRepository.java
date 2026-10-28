package com.example.booking_service.repository;

import com.example.booking_service.common.EventStatus;
import com.example.booking_service.entity.OutBoxEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutBoxEventRepository  extends JpaRepository<OutBoxEvents,String> {

    List<OutBoxEvents> findTop100ByStatusOrderByCreatedAtAsc(EventStatus status);
}
