package com.example.showservice.repository;

import com.example.showservice.entity.MovieShow;
import com.example.showservice.entity.ShowStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovieShowRepository extends JpaRepository<MovieShow, Long> {
    @Query("select s from MovieShow s where (:movieId is null or s.movieId=:movieId) and (:theatreId is null or s.theatreId=:theatreId) and s.status=:status and s.startTime>=:from and s.startTime<:to order by s.startTime asc")
    List<MovieShow> searchShows(@Param("movieId") Long movieId, @Param("theatreId") Long theatreId, @Param("status") ShowStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<MovieShow> findByScreenIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(Long screenId, ShowStatus status, LocalDateTime proposedEnd, LocalDateTime proposedStart);
}
