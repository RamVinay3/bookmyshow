# Theatre Service

Spring Boot microservice for managing theatres, screens, and physical seat layouts. It uses Oracle Database and registers with Eureka.

## Compatibility

- Java 21
- Spring Boot 4.1.1
- Spring Cloud 2025.1.0
- Oracle Database (default local service: `FREEPDB1`)
- Eureka Server at `http://localhost:8761/eureka/`

This is a standalone service. If your existing Eureka server or gateway uses a different Spring Boot/Spring Cloud release train, align the versions in `pom.xml` with that project before integrating.

## Responsibilities

- Theatre CRUD
- Screens within a theatre
- Physical seat layout within a screen
- No show-specific availability or bookings yet

A physical `Seat` is part of a `Screen`. Availability belongs to a future `ShowSeat` concept because the same physical seat can be available for one show and booked for another.

## Configure Oracle

Create a dedicated schema/user using your normal Oracle admin workflow, then set these environment variables:

```bash
export THEATRE_DB_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export THEATRE_DB_USERNAME='THEATRE_APP'
export THEATRE_DB_PASSWORD='your-local-password'
export EUREKA_URL='http://localhost:8761/eureka/'
```

The configured username must already exist and have permission to create/alter tables and sequences in its schema. Hibernate uses `ddl-auto: update` for local learning only; use versioned migrations such as Flyway or Liquibase in production.

## Run

```bash
./mvnw spring-boot:run
```

This ZIP does not include a Maven wrapper. Run with installed Maven:

```bash
mvn spring-boot:run
```

Service listens on port `8084`. Health endpoint: `GET http://localhost:8084/actuator/health`.

## API order and examples

### 1. Create a theatre

`POST /api/theatres`

```json
{
  "name": "PVR Central",
  "address": "123 Example Road",
  "city": "Hyderabad"
}
```

Response includes `id`; use it as `THEATRE_ID`.

### 2. List theatres (optionally filter by city)

`GET /api/theatres`

`GET /api/theatres?city=Hyderabad`

### 3. Get one theatre with screens and seats

`GET /api/theatres/{theatreId}`

### 4. Update a theatre

`PUT /api/theatres/{theatreId}`

```json
{
  "name": "PVR Central Updated",
  "address": "123 Example Road",
  "city": "Hyderabad"
}
```

### 5. Add a screen to a theatre

`POST /api/theatres/{theatreId}/screens`

```json
{
  "name": "Screen 1"
}
```

Save the returned `id` as `SCREEN_ID`.

### 6. List a theatre's screens

`GET /api/theatres/{theatreId}/screens`

### 7. Get one screen

`GET /api/screens/{screenId}`

### 8. Bulk-create seats by row

`POST /api/screens/{screenId}/seats/bulk`

```json
[
  {
    "rowLabel": "A",
    "seatNumbers": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    "seatType": "REGULAR"
  },
  {
    "rowLabel": "B",
    "seatNumbers": [1, 2, 3, 4, 5, 6],
    "seatType": "PREMIUM"
  },
  {
    "rowLabel": "C",
    "seatNumbers": [1, 2, 3, 4],
    "seatType": "RECLINER"
  }
]
```

Seat types accepted: `REGULAR`, `PREMIUM`, `RECLINER`. Duplicate seat positions are rejected by validation and the Oracle unique constraint.

### 9. List seats in a screen

`GET /api/screens/{screenId}/seats`

### 10. Delete a theatre

`DELETE /api/theatres/{theatreId}`

Returns `204 No Content` when successful. This cascades to screens and seats in this learning model. Do not use this destructive cascade without checking business rules in a production system.

## Full API sequence

1. `POST /api/theatres`
2. `POST /api/theatres/{theatreId}/screens`
3. `POST /api/screens/{screenId}/seats/bulk`
4. `GET /api/screens/{screenId}/seats`
5. `GET /api/theatres/{theatreId}`

## Data model

```text
THEATRES
  1
  |
  | many
SCREENS
  1
  |
  | many
SEATS
```

Unique constraints:
- A screen name must be unique within a theatre.
- A seat position (`rowLabel`, `seatNumber`) must be unique within a screen.

## Important next steps

- Add integration tests using a test Oracle schema or Testcontainers-compatible Oracle setup.
- Add database migrations before sharing the service with a team.
- Configure security and authorization before exposing mutation endpoints.
- Implement Show Service next. Booking availability should be modeled separately per show, not stored in `SEATS`.


## Spring Initializr selections used

- Project: Maven - Groovy (Maven generates a `pom.xml`; the language selection is Java)
- Language: Java
- Spring Boot: 4.1.1
- Group: `com.example`
- Artifact: `theatre-service`
- Package: `com.example.theatreservice` (Java package names cannot contain hyphens)
- Packaging: Jar
- Configuration: Properties
- Java: 21
