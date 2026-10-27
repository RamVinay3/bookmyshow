package com.example.booking_service.common;

public enum BookingSagaStatus {
    SEATS_LOCKED,
    PAYMENT_PENDING,
    PAYMENT_DEBITED,
    CONFIRMATION_PENDING,
    CONFIRMED,
    REFUND_PENDING,
    REFUNDED,
    FAILED,
    PAYMENT_OUTCOME_UNKNOWN
}
