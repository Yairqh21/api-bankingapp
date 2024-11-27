package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

public record AccountResponse(
    String accountNumber,
    BigDecimal balance
) {
}

