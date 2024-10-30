package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

public record DepositAndWithdrawRequest(
    String pin,
    BigDecimal amount
) {

}
