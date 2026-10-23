package com.example.theatreservice.service;

import com.example.theatreservice.dto.*;
import com.example.theatreservice.entity.Screen;
import com.example.theatreservice.entity.Seat;
import com.example.theatreservice.entity.Theatre;
import com.example.theatreservice.exception.ConflictException;
import com.example.theatreservice.exception.ResourceNotFoundException;
import com.example.theatreservice.repository.ScreenRepository;
import com.example.theatreservice.repository.SeatRepository;
import com.example.theatreservice.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TheatreService {
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    public TheatreService(TheatreRepository theatreRepository,
                          ScreenRepository screenRepository,
                          SeatRepository seatRepository) {
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
    }

    public TheatreResponse createTheatre(CreateTheatreRequest request) {
        Theatre theatre = theatreRepository.save(
                new Theatre(request.name().trim(), request.address().trim(), request.city().trim()));
        return toTheatreResponse(theatre, false);
    }

    @Transactional(readOnly = true)
    public List<TheatreResponse> getTheatres(String city) {
        List<Theatre> theatres = (city == null || city.isBlank())
                ? theatreRepository.findAll()
                : theatreRepository.findByCityIgnoreCaseOrderByNameAsc(city.trim());
        return theatres.stream().map(theatre -> toTheatreResponse(theatre, false)).toList();
    }

    @Transactional(readOnly = true)
    public TheatreResponse getTheatre(Long theatreId) {
        return toTheatreResponse(findTheatre(theatreId), true);
    }

    public TheatreResponse updateTheatre(Long theatreId, UpdateTheatreRequest request) {
        Theatre theatre = findTheatre(theatreId);
        theatre.updateDetails(request.name().trim(), request.address().trim(), request.city().trim());
        return toTheatreResponse(theatre, false);
    }

    public void deleteTheatre(Long theatreId) {
        Theatre theatre = findTheatre(theatreId);
        theatreRepository.delete(theatre);
    }

    public ScreenResponse createScreen(Long theatreId, CreateScreenRequest request) {
        Theatre theatre = findTheatre(theatreId);
        if (screenRepository.existsByTheatreIdAndNameIgnoreCase(theatreId, request.name().trim())) {
            throw new ConflictException("A screen with this name already exists in this theatre.");
        }
        Screen screen = new Screen(request.name().trim());
        theatre.addScreen(screen);
        screenRepository.save(screen);
        return toScreenResponse(screen, true);
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreens(Long theatreId) {
        findTheatre(theatreId);
        return screenRepository.findByTheatreIdOrderByIdAsc(theatreId)
                .stream().map(screen -> toScreenResponse(screen, false)).toList();
    }

    @Transactional(readOnly = true)
    public ScreenResponse getScreen(Long screenId) {
        return toScreenResponse(findScreen(screenId), true);
    }

    public List<SeatResponse> createSeats(Long screenId, List<SeatRowRequest> rows) {
        Screen screen = findScreen(screenId);
        List<Seat> seats = rows.stream()
                .flatMap(row -> row.seatNumbers().stream().map(number -> {
                    if (number <= 0) {
                        throw new IllegalArgumentException("Seat numbers must be positive.");
                    }
                    if (seatRepository.existsByScreen_IdAndRowLabelIgnoreCaseAndSeatNumber(
                            screenId, row.rowLabel().trim(), number)) {
                        throw new ConflictException("Seat " + row.rowLabel() + number + " already exists.");
                    }
                    return new Seat(row.rowLabel().trim().toUpperCase(), number, row.seatType());
                }))
                .toList();

        seats.forEach(screen::addSeat);
        seatRepository.saveAll(seats);
        return seats.stream().map(this::toSeatResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(Long screenId) {
        findScreen(screenId);
        return seatRepository.findByScreen_IdOrderByRowLabelAscSeatNumberAsc(screenId)
                .stream().map(this::toSeatResponse).toList();
    }

    private Theatre findTheatre(Long id) {
        return theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre " + id + " was not found."));
    }

    private Screen findScreen(Long id) {
        return screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen " + id + " was not found."));
    }

    private TheatreResponse toTheatreResponse(Theatre theatre, boolean includeScreens) {
        List<ScreenResponse> screens = includeScreens
                ? theatre.getScreens().stream().map(screen -> toScreenResponse(screen, true)).toList()
                : List.of();
        return new TheatreResponse(theatre.getId(), theatre.getName(), theatre.getAddress(),
                theatre.getCity(), theatre.getCreatedAt(), screens);
    }

    private ScreenResponse toScreenResponse(Screen screen, boolean includeSeats) {
        List<SeatResponse> seats = includeSeats
                ? screen.getSeats().stream().map(this::toSeatResponse).toList()
                : List.of();
        return new ScreenResponse(screen.getId(), screen.getTheatre().getId(), screen.getName(),
                screen.getSeats().size(), seats);
    }

    private SeatResponse toSeatResponse(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getScreenId(), seat.getRowLabel(),
                seat.getSeatNumber(), seat.getSeatType());
    }
}
