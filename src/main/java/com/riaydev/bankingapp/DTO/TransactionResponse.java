package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;
import java.util.Date;


public record TransactionResponse(
    Long id,
    BigDecimal amount,
    String transactionType, 
    Date transactionDate,
    String sourceAccountNumber,
    String targetAccountNumber
) {
}

