package com.cimhub.authservice.dto.request;

import com.cimhub.authservice.controller.AuthApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "LoginRequest",
        description = "User login credentials",
        example = AuthApiExamples.LOGIN_REQUEST
)
public record LoginRequest(
        @Schema(description = "User's email address", example = "john.driver@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format") String email,

        @Schema(description = "User's password", example = "SecurePassword123!")
        @NotBlank(message = "Password is required") String password) {
}
