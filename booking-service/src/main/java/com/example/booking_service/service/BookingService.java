package com.example.booking_service.service;

import com.example.booking_service.dto.request.SeatLockRequest;
import com.example.booking_service.dto.response.*;
import com.example.booking_service.entity.ShowSeat;
import com.example.booking_service.repository.ShowSeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {

    private final ShowSeatRepository seatRepository;
    private final ShowClient showClient;
    private final TheatreClient theatreClient;

    public BookingService(
            ShowSeatRepository seatRepository,
            ShowClient showClient,
            TheatreClient theatreClient) {
        this.seatRepository = seatRepository;
        this.showClient = showClient;
        this.theatreClient = theatreClient;
    }

    @Transactional
    public List<ShowSeatResponse> getSeats(Long showId) {
        ShowDetails show = showClient.getShow(showId);

        if (show == null || !"SCHEDULED".equals(show.status())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Scheduled show not found");
        }

        initializeSeatsIfRequired(show);

        seatRepository.releaseExpiredLocks(showId, LocalDateTime.now());

        return seatRepository
                .findByShowIdOrderByRowLabelAscSeatNumberAsc(showId)
                .stream()
                .map(ShowSeatResponse::from)
                .toList();
    }

    private void initializeSeatsIfRequired(ShowDetails show) {

        if (seatRepository.countByShowId(show.id()) > 0) {
            return;
        }
        List<ShowSeat> existing =
                seatRepository.findByShowIdOrderByRowLabelAscSeatNumberAsc(
                        show.id());

        if (!existing.isEmpty()) {
            return;
        }

        List<PhysicalSeat> physicalSeats =
                theatreClient.getSeats(show.screenId());

        if (physicalSeats == null || physicalSeats.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No physical seats are configured for this screen");
        }

        List<ShowSeat> showSeats = physicalSeats.stream()
                .map(seat -> new ShowSeat(
                        show.id(),
                        seat.id(),
                        seat.rowLabel(),
                        seat.seatNumber()))
                .toList();

        seatRepository.saveAll(showSeats);
        seatRepository.flush();
    }

    @Transactional
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        List<Long> seatIds = request.seatIds();

        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Duplicate seat IDs are not allowed");
        }

        ShowDetails show = showClient.getShow(request.showId());

        if (show == null || !"SCHEDULED".equals(show.status())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Scheduled show not found");
        }

        initializeSeatsIfRequired(show);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(5);

        for (Long seatId : seatIds) {
            int updated = seatRepository.lockSeat(
                    request.showId(),
                    seatId,
                    request.userId(),
                    expiry,
                    now
            );

            if (updated != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Seat " + seatId
                                + " is unavailable. Refresh the seat map.");
            }
        }

        return new SeatLockResponse(
                request.showId(),
                seatIds,
                expiry,
                "Seats locked successfully"
        );
    }

    @Transactional
    public void releaseSeats(SeatLockRequest request) {
        seatRepository.releaseUserLocks(
                request.showId(),
                request.seatIds(),
                request.userId()
        );
    }
}