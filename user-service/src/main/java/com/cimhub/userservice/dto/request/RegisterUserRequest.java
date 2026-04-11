package com.cimhub.userservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;

@Schema(name = "RegisterUserRequest", description = "Request to register a new user in the system")
public record RegisterUserRequest(
        @Schema(description = "User's full name", example = "John Driver") @NotBlank(message = "Full name is required")
        String fullName,

        @Schema(description = "User's email address", example = "john.driver@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User's phone number", example = "+49 123 456789")
        String phoneNumber,

        @Schema(description = "User's age", example = "32") int age,

        @Schema(description = "User's date of birth") Date dob,

        @Schema(description = "Employee ID (if applicable)", example = "EMP-001")
        String empId,

        @Schema(description = "Organization name (if applicable)", example = "Acme Corp")
        String organisationName,

        @Schema(description = "URL to user's profile picture")
        String profilePicUrl) {}
