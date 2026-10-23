package com.example.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateWalletRequest(@NotNull @Positive Long userId) {}
