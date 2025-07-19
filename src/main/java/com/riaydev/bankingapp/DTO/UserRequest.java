package com.riaydev.bankingapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(

    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "No empty fields")
    //@Email(message = "The email format must be valid.")
    String email,

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{9}$", message = "The phone number must be 9 digits.")
    String phoneNumber,

    @NotBlank(message = "Address is required")
    String address,

    String accountNumber,

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Z]).*$", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = "^(?=.*\\d).*$", message = "Password must contain at least one digit")
    @Pattern(regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).*$", message = "Password must contain at least one special character")
    @Pattern(regexp = "^[^\\s]+$", message = "Password cannot contain whitespace")
    @Size(max = 128, message = "Password must be less than 128 characters long")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @JsonProperty("hashedPassword")
    String password
) {

}
