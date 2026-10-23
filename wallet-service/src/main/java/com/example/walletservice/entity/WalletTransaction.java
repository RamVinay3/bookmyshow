package com.example.walletservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "WALLET_TRANSACTIONS", uniqueConstraints = {
    @UniqueConstraint(name = "UK_WALLET_TXN_REFERENCE_TYPE", columnNames = {"REFERENCE_ID", "TYPE"})
})
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "WALLET_ID", nullable = false)
    private Long walletId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "BALANCE_AFTER", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "REFERENCE_ID", nullable = false, length = 100)
    private String referenceId;

    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected WalletTransaction() {}

    public WalletTransaction(Long walletId, Long userId, TransactionType type, BigDecimal amount,
                             BigDecimal balanceAfter, String referenceId, String description) {
        this.walletId = walletId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceId = referenceId;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getWalletId() { return walletId; }
    public Long getUserId() { return userId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getReferenceId() { return referenceId; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
