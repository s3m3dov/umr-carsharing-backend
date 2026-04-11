package com.cimhub.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for password reset request")
public record ResetPasswordResponse(
        @Schema(description = "Success message", example = "Password reset link sent to your email")
        String message) {}
