package com.example.walletservice.dto;

import com.example.walletservice.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResponse(Long id, Long walletId, Long userId, TransactionType type,
        BigDecimal amount, BigDecimal balanceAfter, String referenceId, String description,
        LocalDateTime createdAt) {}
