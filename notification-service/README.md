# Notification Service

Kafka consumer for the BookMyShow `booking-confirmed` topic.

## Configuration
- Application port: `8088`
- Kafka bootstrap: `localhost:9092` by default
- Consumer group: `notification-service`
- Eureka: `http://localhost:8761/eureka/` by default

Environment overrides:
- `SERVER_PORT`
- `KAFKA_BOOTSTRAP_SERVERS`
- `EUREKA_URL`

## Run
From this directory:

```bash
mvn spring-boot:run
```

Start Kafka and Eureka first. Then start this service and create a successful booking through Booking Service. The consumer should log the received event.

The event DTO's fields must match the JSON published by Booking Service.
