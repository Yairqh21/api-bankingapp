package com.riaydev.bankingapp.DTO;

public record ResetPasswordRequest(

     String identifier,
     String resetToken,
     String newPassword
) {}
