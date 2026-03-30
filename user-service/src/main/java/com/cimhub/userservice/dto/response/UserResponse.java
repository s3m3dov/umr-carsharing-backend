package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "UserResponse", description = "User profile information")
public record UserResponse(
        @Schema(description = "Unique user identifier", example = "user_123")
        String userId,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Driver")
        String lastName,

        @Schema(description = "User's email address", example = "john.driver@example.com")
        String email,

        @Schema(description = "User's role in the system", example = "DRIVER")
        UserType userType,

        @Schema(description = "User's phone number", example = "+49 123 456789")
        String phoneNumber,

        @Schema(description = "User's age", example = "32")
        int age,

        @Schema(description = "Driver's license number", example = "LD-123456789")
        String licenseNumber,

        @Schema(description = "Record creation timestamp (UTC)", example = "2026-03-30T16:48:24.233Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,

        @Schema(description = "Record last update timestamp (UTC)", example = "2026-03-30T16:48:24.233Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt) {
}
