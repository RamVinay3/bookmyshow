package com.example.walletservice.repository;

import com.example.walletservice.entity.TransactionType;
import com.example.walletservice.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByReferenceIdAndType(String referenceId, TransactionType type);
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    
}
