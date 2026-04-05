package com.cimhub.authservice.dto.request;

import com.cimhub.authservice.controller.AuthApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "ValidateTokenRequest",
        description = "Token validation request",
        example = AuthApiExamples.VALIDATE_TOKEN_REQUEST
)
public record ValidateTokenRequest(
        @Schema(
                description = "JWT token to validate",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @NotBlank(message = "Token is required") String token) {
}
