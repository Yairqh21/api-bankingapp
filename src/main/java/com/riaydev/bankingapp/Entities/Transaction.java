package com.riaydev.bankingapp.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @ManyToOne(targetEntity = Account.class)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;  

    @ManyToOne(targetEntity = Account.class)
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;  

    public enum TransactionType {
        CASH_WITHDRAWAL,
        CASH_DEPOSIT,
        CASH_TRANSFER,
        SUBSCRIPTION,
        ASSET_PURCHASE,
        ASSET_SELL
    }
}

