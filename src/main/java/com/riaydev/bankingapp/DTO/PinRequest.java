package com.riaydev.bankingapp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PinRequest(
    @NotBlank @Size(min = 4, max = 4, message = "PIN must be 4 digits")
    String pin
) {}
