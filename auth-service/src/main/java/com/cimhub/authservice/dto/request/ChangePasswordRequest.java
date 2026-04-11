package com.cimhub.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to change password for an authenticated user")
public record ChangePasswordRequest(
        @Schema(description = "Current password", example = "OldPassword123!")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New password", example = "NewPassword123!")
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword) {}
