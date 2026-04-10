package com.codewithpcodes.salima.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgottenPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email format should be valid")
        String email
) {
}
