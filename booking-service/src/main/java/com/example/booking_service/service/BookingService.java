package com.example.booking_service.service;

import com.example.booking_service.common.SeatType;
import com.example.booking_service.dto.request.SeatLockRequest;
import com.example.booking_service.dto.response.*;
import com.example.booking_service.entity.SeatStatus;
import com.example.booking_service.entity.ShowSeat;
import com.example.booking_service.repository.ShowSeatRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class BookingService {

    private final ShowSeatRepository seatRepository;
    private final ShowClient showClient;
    private final TheatreClient theatreClient;
    private final WalletClient walletClient;



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
        List<SeatTypePrice> configuredPrices =
                showClient.getSeatPrices(show.id());

        log.info(configuredPrices +" configured Prices");

        if (configuredPrices == null || configuredPrices.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No seat prices configured for this show"
            );
        }
        Map<String, BigDecimal> priceBySeatType =
                configuredPrices.stream()
                        .collect(Collectors.toMap(
                                SeatTypePrice::seatType,
                                SeatTypePrice::price
                        ));

        List<ShowSeat> showSeats = physicalSeats.stream()
                .map(seat -> {
                    String seatType = seat.seatType();

                    java.math.BigDecimal price =
                            priceBySeatType.get(seatType);

                    if (price == null) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "No price configured for seat type "
                                        + seatType
                                        + " for show "
                                        + show.id()
                        );
                    }

                    return new ShowSeat(
                            show.id(),
                            seat.id(),
                            seat.rowLabel(),
                            seat.seatNumber(),
                            SeatType.valueOf(
                                    seatType
                            ),
                            price,
                            null
                    );
                })
                .toList();
        log.info(showSeats+ " show seats");
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
        String bookingId = UUID.randomUUID().toString();

        for (Long seatId : seatIds) {
            int updated = seatRepository.lockSeat(
                    request.showId(),
                    seatId,
                    request.userId(),
                    bookingId,
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
                bookingId,
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

    @Transactional
    public void confirmBooking(
            String bookingId,
            Long showId,
            List<Long> seatIds
    ) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one seat is required"
            );
        }

        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate seat IDs are not allowed"
            );
        }

        // 1. Lock all requested seat rows.
        List<ShowSeat> seats =
                seatRepository.findSeatsForUpdate(showId, seatIds);

        if (seats.size() != seatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "One or more seats were not found for this show"
            );
        }

        // 2. If every seat is already booked for this booking,
        //    treat this request as an idempotent retry.
        boolean alreadyBooked = seats.stream().allMatch(seat ->
                seat.getStatus() == SeatStatus.BOOKED
                        && Objects.equals(seat.getBookingId(), bookingId)
        );

        if (alreadyBooked) {
            return;
        }

        // 3. Verify that every seat is still locked by this booking.
        LocalDateTime now = LocalDateTime.now();

        boolean validLocks = seats.stream().allMatch(seat ->
                seat.getStatus() == SeatStatus.LOCKED
                        && Objects.equals(seat.getBookingId(), bookingId)
                        && seat.getLockedUntil() != null
                        && seat.getLockedUntil().isAfter(now)
        );

        if (!validLocks) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seats are not locked by this booking or their locks have expired"
            );
        }

        // 4. Confirm each seat using your existing conditional update.
        for (Long seatId : seatIds) {
            int updated = seatRepository.confirmSeat(
                    showId,
                    seatId,
                    bookingId,
                    now
            );

            if (updated != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Could not confirm seat " + seatId
                );
            }
        }
    }

    public BigDecimal calculateTotalPrice(
            Long showId,
            List<Long> seatIds,
            String userId,
            String bookingId) {

        if (seatIds == null || seatIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one seat is required"
            );
        }

        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate seat IDs are not allowed"
            );
        }

        List<ShowSeat> seats = seatRepository.findSeatsLockedForBooking(
                showId,
                seatIds,
                userId,
                bookingId,
                LocalDateTime.now()
        );

        if (seats.size() != seatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Some seats are no longer locked by this booking"
            );
        }

        return seats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void releaseBookingLocks(
            Long showId,
            List<Long> seatIds,
            String userId,
            String bookingId) {

        seatRepository.releaseBookingLocks(
                showId,
                seatIds,
                userId,
                bookingId
        );
    }
}