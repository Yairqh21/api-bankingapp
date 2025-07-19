package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record TransactionResponse(
    Long id,
    BigDecimal amount,
    String transactionType, 
    LocalDateTime transactionDate,
    String sourceAccountNumber,
    String targetAccountNumber
) {
}

