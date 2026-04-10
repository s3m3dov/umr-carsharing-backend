package com.cimhub.authservice.dto.request;

import com.cimhub.authservice.dto.examples.AuthApiDoc;
import com.cimhub.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(
        name = "RegisterUserRequest",
        description = "User registration request containing personal information",
        example = AuthApiDoc.SIGNUP_REQUEST
)
public record RegisterUserRequest(
        @Schema(description = "User's first name", example = "John")
        @NotBlank(message = "First name is required") String firstName,

        @Schema(description = "User's last name", example = "Driver")
        @NotBlank(message = "Last name is required") String lastName,

        @Schema(description = "User's email address (unique identifier)", example = "john.driver@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format") String email,

        @Schema(description = "User's password (8+ chars, uppercase, lowercase, digit, special)", example = "SecurePassword123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters") String password,

        @Schema(description = "User type: DRIVER or PASSENGER", example = "DRIVER")
        @NotNull(message = "User type is required") UserType userType,

        @Schema(description = "User's contact phone number (optional)", example = "+49 123 456789")
        String phoneNumber,

        @Schema(description = "User's age (optional, must be positive)", example = "32")
        @Positive(message = "Age must be positive") Integer age,

        @Schema(description = "Driver's license number (required for DRIVER, optional for PASSENGER)", example = "LD-123456789")
        String licenseNumber) {
}
