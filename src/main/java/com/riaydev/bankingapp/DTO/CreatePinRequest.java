package com.riaydev.bankingapp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePinRequest(
    @NotBlank(message = "The field cannot be empty.")
    @Pattern(regexp = "^[0-9]{4}$", message = "The pin number must be exactly 4 digits.")
    String pin,

    @NotBlank(message = "The field cannot be empty.")
    String password
) {

}
