# Show Service

Java 21 · Spring Boot 4.1.1 · Maven · Oracle · Eureka · Spring Cloud LoadBalancer

## Setup

Configure environment variables for a dedicated Oracle schema and your Eureka server:

```bash
export SHOW_DB_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export SHOW_DB_USERNAME='SHOW_APP'
export SHOW_DB_PASSWORD='your-local-password'
export EUREKA_URL='http://localhost:8761/eureka/'
```

Ensure `SHOW_APP` exists and can create/alter tables in its own schema. `ddl-auto=update` is for local learning; use
migrations in production. Start Eureka, Movie Service, and Theatre Service first, then run:

```bash
mvn spring-boot:run
```

Port: `8085`; health: `GET /actuator/health`.

## APIs in order

1. `POST /api/shows` — create a show:

```json
{
  "movieId": 1,
  "theatreId": 1,
  "screenId": 1,
  "startTime": "2027-01-15T18:00:00",
  "endTime": "2027-01-15T20:30:00",
  "language": "Telugu",
  "format": "2D"
}
```

Use existing IDs from Movie and Theatre services. The Movie client expects `GET /api/movies/{id}`. Theatre client
expects `GET /api/theatres/{id}` and `GET /api/screens/{id}`, whose response must contain `id` and `theatreId`.

2. `GET /api/shows/{showId}` — retrieve one show.
3. `GET /api/shows?date=2027-01-15` — list scheduled shows for a date.
4. `GET /api/shows?movieId=1&date=2027-01-15` — filter by movie.
5. `GET /api/shows?theatreId=1&date=2027-01-15` — filter by theatre.
6. `PUT /api/shows/{showId}` — update start/end, language and format:

```json
{
  "startTime": "2027-01-15T18:30:00",
  "endTime": "2027-01-15T21:00:00",
  "language": "Telugu",
  "format": "2D"
}
```

7. `DELETE /api/shows/{showId}` — logical cancellation (status becomes CANCELLED).

## Important design notes

- A screen cannot have overlapping scheduled shows according to a read-before-write validation. Concurrent show creation
  can still race; production systems need a locking/constraint strategy and buffer/cleanup time.
- Times use `LocalDateTime`; agree on timezone convention across services. Consider storing UTC instants for production.
- This service stores only IDs for movie/theatre/screen and never reads another service's database.
- Version compatibility: align Spring Boot and Spring Cloud versions with the existing Eureka server and other services
  if needed.
- This first version does not reserve seats; availability belongs to a future show-specific seat/booking model.
