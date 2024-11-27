package com.riaydev.bankingapp.DTO;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
    @NotBlank(message = "Identifier cannot be empty.")
    String identifier
) {

}
