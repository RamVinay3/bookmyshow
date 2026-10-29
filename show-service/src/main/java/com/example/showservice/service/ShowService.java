package com.example.showservice.service;

import com.example.showservice.client.*;
import com.example.showservice.dto.*;
import com.example.showservice.entity.*;
import com.example.showservice.exception.*;
import com.example.showservice.repository.MovieShowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
@Slf4j
@Service
@Transactional
public class ShowService {
    private final MovieShowRepository repo;
    private final MovieClient movieClient;
    private final TheatreClient theatreClient;

    public ShowService(MovieShowRepository repo, MovieClient movieClient, TheatreClient theatreClient) {
        this.repo = repo;
        this.movieClient = movieClient;
        this.theatreClient = theatreClient;
    }

    public ShowResponse create(CreateShowRequest r) {
        validateTimes(r.startTime(), r.endTime());
        movieClient.assertMovieExists(r.movieId());
        theatreClient.assertTheatreExists(r.theatreId());
        theatreClient.assertScreenBelongsToTheatre(r.theatreId(), r.screenId());
        ensureNoOverlap(r.screenId(), r.startTime(), r.endTime(), null);
        return response(repo.save(new MovieShow(r.movieId(), r.theatreId(), r.screenId(), r.startTime(), r.endTime(), r.language().trim(), r.format().trim().toUpperCase())));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "showDetails", key = "#id")
    public ShowResponse get(Long id) {
        log.info("Method Call -id: {}",id);
        return response(find(id));
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> search(Long movieId, Long theatreId, LocalDate date) {
        LocalDate d = date == null ? LocalDate.now() : date;
        return repo.searchShows(movieId, theatreId, ShowStatus.SCHEDULED, d.atStartOfDay(), d.plusDays(1).atStartOfDay()).stream().map(this::response).toList();
    }

    @CacheEvict(value = "showDetails", key = "#id")
    public ShowResponse update(Long id, UpdateShowRequest r) {
        MovieShow s = find(id);
        if (s.getStatus() == ShowStatus.CANCELLED) throw new ConflictException("A cancelled show cannot be updated.");
        validateTimes(r.startTime(), r.endTime());
        ensureNoOverlap(s.getScreenId(), r.startTime(), r.endTime(), id);
        s.updateSchedule(r.startTime(), r.endTime(), r.language().trim(), r.format().trim().toUpperCase());
        return response(s);
    }

    public ShowResponse cancel(Long id) {
        MovieShow s = find(id);
        s.cancel();
        return response(s);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) throw new IllegalArgumentException("endTime must be after startTime.");
    }

    private void ensureNoOverlap(Long screenId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        boolean overlap = repo.findByScreenIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(screenId, ShowStatus.SCHEDULED, end, start).stream().anyMatch(s -> !s.getId().equals(excludeId));
        if (overlap) throw new ConflictException("This screen already has a scheduled show overlapping that time.");
    }

    private MovieShow find(Long id) {

        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Show " + id + " was not found."));
    }

    private ShowResponse response(MovieShow s) {
        return new ShowResponse(s.getId(), s.getMovieId(), s.getTheatreId(), s.getScreenId(), s.getStartTime(), s.getEndTime(), s.getLanguage(), s.getFormat(), s.getStatus(), s.getCreatedAt());
    }
}
