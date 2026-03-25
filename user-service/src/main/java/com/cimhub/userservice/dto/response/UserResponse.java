package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

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
        String licenseNumber) {
}
