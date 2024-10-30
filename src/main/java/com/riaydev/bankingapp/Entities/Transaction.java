package com.riaydev.bankingapp.Entities;

import java.math.BigDecimal;
import java.util.Date;


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

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    private TransactionType transactionType;
    private Date transactionDate;
    private String sourceAccountNumber;
    private String targetAccountNumber;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public enum TransactionType {
        CASH_WITHDRAWAL,
        CASH_DEPOSIT,
        CASH_TRANSFER,
        SUBSCRIPTION,
        ASSET_PURCHASE,
        ASSET_SELL
    }
    
}
