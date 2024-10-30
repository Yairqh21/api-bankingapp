package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

public record TransferRequest(
        String pin,
        BigDecimal amount,
        String targetAccountNumber) {

}
