package com.codewithpcodes.salima.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgottenPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email format should be valid")
        String email,
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password should be at least 6 characters long")
        String newPassword,
        @NotBlank(message = "Confirm new password is required")
        @Size(min = 6, message = "Password should be at least 6 characters long")
        String confirmNewPassword
) {
}
