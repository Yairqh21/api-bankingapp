package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;
import java.util.Date;

import com.riaydev.bankingapp.Entities.Transaction;

public record TransactionResponse(
    Long id,
    BigDecimal amount,
    String transactionType, 
    Date transactionDate,
    String sourceAccountNumber,
    String targetAccountNumber
) {
    public TransactionResponse(Transaction transaction) {
        this(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getTransactionType().toString(), 
            transaction.getTransactionDate(),
            transaction.getSourceAccountNumber(),
            transaction.getTargetAccountNumber()
        );
    }
}

