package com.example.walletservice.service;

import com.example.walletservice.dto.AmountRequest;
import com.example.walletservice.dto.CreateWalletRequest;
import com.example.walletservice.dto.WalletResponse;
import com.example.walletservice.dto.WalletTransactionResponse;
import com.example.walletservice.entity.TransactionType;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.entity.WalletTransaction;
import com.example.walletservice.exception.ConflictException;
import com.example.walletservice.exception.InsufficientBalanceException;
import com.example.walletservice.exception.ResourceNotFoundException;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public WalletResponse create(CreateWalletRequest request) {
        if (walletRepository.existsByUserId(request.userId())) {
            throw new ConflictException("Wallet already exists for userId " + request.userId());
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(request.userId());
        wallet.setBalance(BigDecimal.ZERO.setScale(2));
        return toResponse(walletRepository.save(wallet));
    }

    @Transactional(readOnly = true)
    public WalletResponse getByUserId(Long userId) {
        return toResponse(walletRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for userId " + userId)));
    }

    @Transactional
    public WalletTransactionResponse topUp(Long userId, AmountRequest request) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for userId " + userId));
        if (transactionRepository.findByReferenceIdAndType(request.referenceId(), TransactionType.TOP_UP).isPresent()) {
            throw new ConflictException("Top-up referenceId has already been used: " + request.referenceId());
        }
        BigDecimal amount = money(request.amount());
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        return toTransaction(transactionRepository.save(new WalletTransaction(wallet.getId(), userId,
            TransactionType.TOP_UP, amount, wallet.getBalance(), request.referenceId(), request.description())));
    }

    @Transactional
    public WalletTransactionResponse debitForBooking(Long userId, AmountRequest request) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for userId " + userId));

        var existing = transactionRepository.findByReferenceIdAndType(request.referenceId(), TransactionType.BOOKING_DEBIT);
        if (existing.isPresent()) {
            WalletTransaction previous = existing.get();
            if (!previous.getUserId().equals(userId) || previous.getAmount().compareTo(money(request.amount())) != 0) {
                throw new ConflictException("This booking reference was already processed with different details.");
            }
            return toTransaction(previous); // Idempotent retry: do not debit twice.
        }

        BigDecimal amount = money(request.amount());
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance. Available: " + wallet.getBalance());
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        return toTransaction(transactionRepository.save(new WalletTransaction(wallet.getId(), userId,
            TransactionType.BOOKING_DEBIT, amount, wallet.getBalance(), request.referenceId(), request.description())));
    }

    @Transactional
    public WalletTransactionResponse refundBooking(Long userId, AmountRequest request) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for userId " + userId));
        var debit = transactionRepository.findByReferenceIdAndType(request.referenceId(), TransactionType.BOOKING_DEBIT)
            .orElseThrow(() -> new ResourceNotFoundException("No booking debit found for referenceId " + request.referenceId()));
        if (!debit.getUserId().equals(userId)) {
            throw new ConflictException("Booking reference does not belong to this user.");
        }
        var existingRefund = transactionRepository.findByReferenceIdAndType(request.referenceId(), TransactionType.BOOKING_REFUND);
        if (existingRefund.isPresent()) return toTransaction(existingRefund.get());
        if (money(request.amount()).compareTo(debit.getAmount()) != 0) {
            throw new ConflictException("Refund amount must match the original booking debit.");
        }
        BigDecimal amount = debit.getAmount();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        return toTransaction(transactionRepository.save(new WalletTransaction(wallet.getId(), userId,
            TransactionType.BOOKING_REFUND, amount, wallet.getBalance(), request.referenceId(), request.description())));
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> history(Long userId) {
        if (!walletRepository.existsByUserId(userId)) {
            throw new ResourceNotFoundException("Wallet not found for userId " + userId);
        }
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toTransaction).toList();
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, java.math.RoundingMode.UNNECESSARY); }
    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getUserId(), wallet.getBalance(), wallet.getCreatedAt(), wallet.getUpdatedAt());
    }
    private WalletTransactionResponse toTransaction(WalletTransaction tx) {
        return new WalletTransactionResponse(tx.getId(), tx.getWalletId(), tx.getUserId(), tx.getType(),
            tx.getAmount(), tx.getBalanceAfter(), tx.getReferenceId(), tx.getDescription(), tx.getCreatedAt());
    }
}
