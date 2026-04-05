package com.cimhub.userservice.dto.request;

import com.cimhub.userservice.controller.UserApiExamples;
import com.cimhub.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Schema(
        name = "CreateProfileRequest",
        description = "Request to create or update user profile",
        example = UserApiExamples.CREATE_PROFILE_REQUEST
)
public record CreateProfileRequest(
        @Schema(description = "User's unique identifier", example = "john.driver@example.com")
        @NotBlank(message = "User ID is required") String userId,

        @Schema(description = "User's email address", example = "john.driver@example.com")
        @NotBlank(message = "Email is required") String email,

        @Schema(description = "User type: DRIVER or PASSENGER", example = "DRIVER")
        @NotNull(message = "UserType is required") UserType userType,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Driver")
        String lastName,

        @Schema(description = "User's phone number", example = "+49 123 456789")
        String phoneNumber,

        @Schema(description = "User's age", example = "32")
        int age,

        @Schema(description = "Driver's license number (required for DRIVER user type)", example = "LD-123456789")
        String licenseNumber) {
}