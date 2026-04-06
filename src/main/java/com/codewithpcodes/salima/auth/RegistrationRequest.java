package com.codewithpcodes.salima.auth;

import com.codewithpcodes.salima.user.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistrationRequest(
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

        @NotBlank(message = "National ID is required")
        String nationalId,

        @NotBlank(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Region is required")
        String region,

        @NotBlank(message = "Date of birth is required")
        LocalDate dateOfBirth
) {
}
