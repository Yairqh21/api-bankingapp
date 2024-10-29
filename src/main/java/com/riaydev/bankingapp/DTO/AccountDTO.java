package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

public record AccountDTO(
    String accountNumber,
    BigDecimal balance
) {
}

