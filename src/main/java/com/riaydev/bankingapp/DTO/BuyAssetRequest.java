package com.riaydev.bankingapp.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BuyAssetRequest(

     @NotBlank(message = "Asset symbol cannot be empty.")
     String assetSymbol,

     @NotBlank(message = "Pin cannot be empty.")
     @Pattern(regexp = "^[0-9]{4}$", message = "Pin must be exactly 4 digits.")
     String pin,
     
     @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero.")
     BigDecimal amount

) {
    
}
