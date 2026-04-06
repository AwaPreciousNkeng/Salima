package com.codewithpcodes.salima.auth;

import com.codewithpcodes.salima.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAdminRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password should be at least 6 characters long")
        String password,

        @NotBlank(message = "Phone number is required")
        @Size(min = 9, message = "Phone number should be 9 characters long")
        String phoneNumber,

        @NotBlank(message = "Date of birth is required")
        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth
) {
}
