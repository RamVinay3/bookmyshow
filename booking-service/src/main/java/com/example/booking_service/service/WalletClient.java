package com.example.booking_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class WalletClient {

    private final WebClient client;

    public WalletClient(
            WebClient.Builder builder,
            @Value("${services.wallet.base-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public WalletTransactionResponse debit(
            Long userId,
            BigDecimal amount,
            String bookingId) {

        AmountRequest request = new AmountRequest(
                amount,
                bookingId,
                "BookMyShow seat booking"
        );

        return client.post()
                .uri("/api/wallets/{userId}/debit", userId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WalletTransactionResponse.class)
                .block();
    }

    public WalletTransactionResponse refund(
            Long userId,
            BigDecimal amount,
            String bookingId) {

        AmountRequest request = new AmountRequest(
                amount,
                bookingId,
                "Refund for failed seat booking"
        );

        return client.post()
                .uri("/api/wallets/{userId}/refund", userId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WalletTransactionResponse.class)
                .block();
    }

    public record AmountRequest(
            BigDecimal amount,
            String referenceId,
            String description) {
    }

    public record WalletTransactionResponse(
            Long id,
            Long walletId,
            Long userId,
            String type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String referenceId,
            String description,
            java.time.LocalDateTime createdAt) {
    }
}