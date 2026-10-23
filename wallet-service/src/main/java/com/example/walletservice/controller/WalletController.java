package com.example.walletservice.controller;

import com.example.walletservice.dto.AmountRequest;
import com.example.walletservice.dto.CreateWalletRequest;
import com.example.walletservice.dto.WalletResponse;
import com.example.walletservice.dto.WalletTransactionResponse;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletService walletService;
    public WalletController(WalletService walletService) { this.walletService = walletService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse create(@Valid @RequestBody CreateWalletRequest request) {
        return walletService.create(request);
    }

    @GetMapping("/{userId}")
    public WalletResponse get(@PathVariable @Positive Long userId) { return walletService.getByUserId(userId); }

    @PostMapping("/{userId}/top-up")
    public WalletTransactionResponse topUp(@PathVariable @Positive Long userId, @Valid @RequestBody AmountRequest request) {
        return walletService.topUp(userId, request);
    }

    @PostMapping("/{userId}/debit")
    public WalletTransactionResponse debit(@PathVariable @Positive Long userId, @Valid @RequestBody AmountRequest request) {
        return walletService.debitForBooking(userId, request);
    }

    @PostMapping("/{userId}/refund")
    public WalletTransactionResponse refund(@PathVariable @Positive Long userId, @Valid @RequestBody AmountRequest request) {
        return walletService.refundBooking(userId, request);
    }

    @GetMapping("/{userId}/transactions")
    public List<WalletTransactionResponse> history(@PathVariable @Positive Long userId) {
        return walletService.history(userId);
    }
}
