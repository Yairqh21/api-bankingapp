package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubscriptionRequest(

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "1.00", inclusive = false, message = "Amount must be positive")
    BigDecimal amount,
    @NotNull
    @Positive(message = "Amount must be positive")
    Integer intervalSeconds,
    @NotNull
    @Positive(message = "Interval must be positive")
    String pin
) {

}
