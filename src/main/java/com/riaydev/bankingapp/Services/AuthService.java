package com.riaydev.bankingapp.Services;

public interface AuthService {

    void sendOtp(String identifier);
    String verifyOtp(String identifier, String otp);
    void resetPassword(String identifier, String resetToken, String newPassword);
       
}

