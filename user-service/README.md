# User Service

A small user-profile service for the BookMyShow-style microservices project. It creates user profiles and stores them in
its own Oracle schema. It uses `application.yml` (not `application.properties`), registers with Eureka, and runs on port
`8087`.

## Stack

- Java 21
- Spring Boot 4.1.1
- Maven
- Oracle Database (`ojdbc11`)
- Spring Data JPA + Bean Validation
- Eureka client

## Before running

1. Make sure Oracle and Eureka Server are running.
2. Create a dedicated Oracle user/schema, for example:

```sql
CREATE USER USER_APP IDENTIFIED BY "your-strong-password";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER TO USER_APP;
ALTER USER USER_APP QUOTA UNLIMITED ON USERS;
```

If your Oracle tablespace isn't named `USERS`, adjust the quota statement accordingly. Grant only the permissions
required by your local schema policy.

3. Update `src/main/resources/application.yml` or set environment variables:

```bash
export USER_DB_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export USER_DB_USERNAME='USER_APP'
export USER_DB_PASSWORD='your-strong-password'
export EUREKA_URL='http://localhost:8761/eureka/'
```

4. From this directory run:

```bash
mvn spring-boot:run
```

Health check: `GET http://localhost:8087/actuator/health`

## API

### Create a user

`POST http://localhost:8087/api/users`

```json
{
  "fullName": "Vinay Kumar",
  "email": "vinay@example.com",
  "phoneNumber": "9876543210"
}
```

Returns `201 Created` with the generated user ID. Email is normalized to lowercase and must be unique.

### Get user by ID

`GET /api/users/{id}`

### List users (paginated)

`GET /api/users?page=0&size=20&sort=id,asc`

### Update user profile

`PUT /api/users/{id}`

```json
{
  "fullName": "Vinay Kumar",
  "email": "vinay@example.com",
  "phoneNumber": "9876543210"
}
```

### Deactivate user

`PATCH /api/users/{id}/deactivate`

## Design notes

- `APP_USERS` is managed by this service; other services should refer to the user by `userId`, not query this database
  directly.
- This service stores profile details only. It intentionally does not store passwords or implement login/authentication;
  that belongs in a dedicated identity/auth service.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for local learning. Use versioned migrations (Flyway/Liquibase)
  for production.
- Confirm that Spring Boot 4.1.1 and Spring Cloud 2025.1.0 match the versions in your existing Eureka setup. The
  generated project has not been built against your local dependency cache/database.
