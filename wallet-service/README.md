# Wallet Service

A demo wallet microservice for the movie-booking project. It uses Java 21, Spring Boot 4.1.1, Maven, Oracle, Eureka, and **`application.yml`** (not `application.properties`). It does not integrate with an external payment provider.

## What it does

- Creates one wallet per user, initially with a zero balance.
- Supports a demo top-up so you can seed wallet funds without payment integration.
- Debits wallet balance for a booking.
- Makes booking debit requests idempotent by `referenceId` so a retried request does not charge twice.
- Refunds a previous booking debit once.
- Stores an append-only transaction history with balance-after values.
- Uses a database row lock for balance-changing operations to reduce concurrent overspending.

> **Demo limitation:** the top-up endpoint directly credits money and must not be exposed as-is in a real product. Real wallets need authenticated authorization, audited funding sources, fraud controls, and stronger operational controls. This is a learning project, not a regulated stored-value wallet.

## Configuration

Edit `src/main/resources/application.yml` or provide environment variables:

- `WALLET_DB_URL` (default `jdbc:oracle:thin:@//localhost:1521/FREEPDB1`)
- `WALLET_DB_USERNAME` (default `WALLET_APP`)
- `WALLET_DB_PASSWORD` (default `change-me`)
- `EUREKA_URL` (default `http://localhost:8761/eureka/`)

Create the Oracle schema user with an appropriately restricted password and grants, for example as a DBA:

```sql
CREATE USER WALLET_APP IDENTIFIED BY "replace-with-a-strong-password";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER TO WALLET_APP;
ALTER USER WALLET_APP QUOTA UNLIMITED ON USERS;
```

Adjust tablespace and grants to match your Oracle setup. The service uses `spring.jpa.hibernate.ddl-auto: update` for local learning; use versioned database migrations for production.

## Run

Start Oracle and Eureka, then run:

```bash
mvn spring-boot:run
```

Service port: `8086`  
Health: `GET http://localhost:8086/actuator/health`

## API examples

### 1. Create a wallet

`POST /api/wallets`

```json
{ "userId": 101 }
```

### 2. Add demo funds

`POST /api/wallets/101/top-up`

```json
{ "amount": 1500.00, "referenceId": "TOPUP-101-001", "description": "Demo wallet funding" }
```

### 3. Check wallet balance

`GET /api/wallets/101`

### 4. Debit for a booking

`POST /api/wallets/101/debit`

```json
{ "amount": 450.00, "referenceId": "BOOKING-9001", "description": "Movie ticket booking" }
```

Use the **same booking reference** for retries. A retry with the same user and amount returns the original debit without deducting the balance again. A retry with different details returns `409 Conflict`.

### 5. Refund a cancelled booking

`POST /api/wallets/101/refund`

```json
{ "amount": 450.00, "referenceId": "BOOKING-9001", "description": "Booking cancelled" }
```

The amount must match the original debit. Repeated refunds for the same reference return the original refund.

### 6. View transaction history

`GET /api/wallets/101/transactions`

## Booking-service integration

When the Booking Service is ready to confirm a booking, it can call the Wallet Service through Eureka using a load-balanced HTTP client at `http://wallet-service/api/wallets/{userId}/debit`. Pass the booking ID as `referenceId`. Only mark the booking as confirmed after the debit succeeds; if booking confirmation fails after the debit, trigger a compensating refund. This is a distributed workflow, not one shared database transaction.

## Notes

- Transaction types: `TOP_UP`, `BOOKING_DEBIT`, `BOOKING_REFUND`.
- Amounts are decimal currency amounts with two decimal places; currency is not modelled in this learning version, so all wallets implicitly use one currency.
- Build/runtime compatibility and live Oracle/Eureka integration have not been verified in your environment.
