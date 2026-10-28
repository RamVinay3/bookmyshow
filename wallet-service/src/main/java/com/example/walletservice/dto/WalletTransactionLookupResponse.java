package com.example.walletservice.dto;

public record WalletTransactionLookupResponse(
        boolean found,
        WalletTransactionResponse transaction
) {
}