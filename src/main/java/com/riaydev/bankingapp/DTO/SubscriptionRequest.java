package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record SubscriptionRequest(

    @NotBlank(message = "")
    BigDecimal amount,
    @NotBlank
    Integer intervalSeconds,
    @NotBlank
    String pin
) {

}
