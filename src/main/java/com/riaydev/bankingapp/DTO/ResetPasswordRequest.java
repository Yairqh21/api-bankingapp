package com.riaydev.bankingapp.DTO;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

          @NotBlank(message = "Identifier cannot be empty.") String identifier,

          @NotBlank(message = "Reset token cannot be empty.") String resetToken,

          @NotBlank(message = "New password cannot be empty.") String newPassword

) {
}
