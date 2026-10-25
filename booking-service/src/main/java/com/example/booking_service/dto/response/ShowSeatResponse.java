package com.example.booking_service.dto.response;

import com.example.booking_service.entity.SeatStatus;
import com.example.booking_service.entity.ShowSeat;
import java.time.LocalDateTime;

public record ShowSeatResponse(
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        SeatStatus status,
        LocalDateTime lockedUntil
) {
    public static ShowSeatResponse from(ShowSeat seat) {
        return new ShowSeatResponse(
                seat.getSeatId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getStatus(),
                seat.getLockedUntil()
        );
    }
}