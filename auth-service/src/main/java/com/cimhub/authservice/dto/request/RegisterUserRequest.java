package com.cimhub.authservice.dto.request;

import com.cimhub.authservice.controller.AuthApiExamples;
import com.cimhub.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "RegisterUserRequest",
        description = "User registration request containing personal information",
        example = AuthApiExamples.SIGNUP_REQUEST
)
public record RegisterUserRequest(
        @Schema(description = "User's first name", example = "John")
        @NotBlank(message = "First name is required") String firstName,

        @Schema(description = "User's last name", example = "Driver")
        @NotBlank(message = "Last name is required") String lastName,

        @Schema(description = "User's email address (unique identifier)", example = "john.driver@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format") String email,

        @Schema(description = "User's password (must be strong)", example = "SecurePassword123!")
        @NotBlank(message = "Password is required") String password,

        @Schema(description = "User type: DRIVER or PASSENGER", example = "DRIVER")
        @NotNull(message = "User type is required") UserType userType,

        @Schema(description = "User's contact phone number (optional)", example = "+49 123 456789")
        String phoneNumber,

        @Schema(description = "User's age (optional)", example = "32")
        int age,

        @Schema(description = "Driver's license number (required for DRIVER, optional for PASSENGER)", example = "LD-123456789")
        String licenseNumber) {
}
