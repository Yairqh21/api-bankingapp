package com.riaydev.bankingapp.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.OtpRequest;
import com.riaydev.bankingapp.DTO.ResetPasswordRequest;

import com.riaydev.bankingapp.DTO.VerifyOtpRequest;
import com.riaydev.bankingapp.Services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/password-reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest request) {
        authService.sendOtp(request.identifier());
        return ResponseEntity.ok(
                Map.of("message", "OTP sent successfully to: " + request.identifier()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String resetToken = authService.verifyOtp(
                request.identifier(),
                request.otp());
        return ResponseEntity.ok(
                Map.of("passwordResetToken", resetToken));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(
                request.identifier(),
                request.resetToken(),
                request.newPassword());
        return ResponseEntity.ok(
                Map.of("message", "Password reset successfully"));
    }
}
