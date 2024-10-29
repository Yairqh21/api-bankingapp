package com.riaydev.bankingapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record UserDTO(

    @NotEmpty(message = "No empty fields")
    String name,

    @NotEmpty(message = "No empty fields")
    @Email(message = "The email format must be valid.")
    String email,

    @NotEmpty(message = "No empty fields")
    @Pattern(regexp = "^[0-9]{9}$", message = "The phone number must be 9 digits.")
    String phoneNumber,

    @NotEmpty(message = "No empty fields")
    String address,

    String accountNumber,

    @NotEmpty(message = "No empty fields")
    @JsonProperty("hashedPassword")
    String password
) {

}
