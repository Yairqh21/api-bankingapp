package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record SellAssetRequest(
        @NotBlank(message = "The field cannot be empty.")
        String assetSymbol,
        @NotBlank(message = "The field cannot be empty.")
        String pin,
        @DecimalMin(value = "0.01", inclusive = true, message = "Quantity must be greater than zero.")
        BigDecimal quantity) {

}
