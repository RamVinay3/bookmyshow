package com.example.showservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIE_SHOWS", indexes = {
        @Index(name = "IDX_SHOW_MOVIE_START", columnList = "MOVIE_ID, START_TIME"),
        @Index(name = "IDX_SHOW_SCREEN_START", columnList = "SCREEN_ID, START_TIME")})
public class MovieShow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "MOVIE_ID", nullable = false)
    private Long movieId;
    @Column(name = "THEATRE_ID", nullable = false)
    private Long theatreId;
    @Column(name = "SCREEN_ID", nullable = false)
    private Long screenId;
    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "LANGUAGE", nullable = false, length = 40)
    private String language;
    @Column(name = "FORMAT", nullable = false, length = 30)
    private String format;
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ShowStatus status;
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected MovieShow() {
    }

    public MovieShow(Long movieId, Long theatreId, Long screenId, LocalDateTime startTime, LocalDateTime endTime, String language, String format) {
        this.movieId = movieId;
        this.theatreId = theatreId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.language = language;
        this.format = format;
        this.status = ShowStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
    }

    public void updateSchedule(LocalDateTime start, LocalDateTime end, String language, String format) {
        this.startTime = start;
        this.endTime = end;
        this.language = language;
        this.format = format;
    }

    public void cancel() {
        this.status = ShowStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Long getTheatreId() {
        return theatreId;
    }

    public Long getScreenId() {
        return screenId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getLanguage() {
        return language;
    }

    public String getFormat() {
        return format;
    }

    public ShowStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
