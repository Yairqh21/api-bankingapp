package com.riaydev.bankingapp.DTO;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
     @NotBlank(message = "The field cannot be empty.")
     String identifier,

     @NotBlank(message = "The field cannot be empty.")
     String otp
) {

}
