package com.cimhub.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to confirm password reset with new password")
public record ConfirmResetPasswordRequest(
        @Schema(description = "Reset token sent to user email", example = "abc123...")
        @NotBlank(message = "Reset token is required")
        String token,

        @Schema(description = "New password", example = "NewPassword123!")
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword) {}
