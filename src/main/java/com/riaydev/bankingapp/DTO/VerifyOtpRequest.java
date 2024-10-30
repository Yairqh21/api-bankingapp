package com.riaydev.bankingapp.DTO;

public record VerifyOtpRequest(
     String identifier,
     String otp
) {

}
