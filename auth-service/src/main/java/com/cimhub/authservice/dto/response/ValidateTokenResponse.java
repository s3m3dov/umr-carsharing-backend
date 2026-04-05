package com.cimhub.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ValidateTokenResponse",
        description = "Token validation response containing validation status and user information",
        example = """
                {
                  "isValid": true,
                  "email": "john.driver@example.com",
                  "role": "DRIVER"
                }
                """
)
public record ValidateTokenResponse(
        @Schema(
                description = "Whether the token is valid and not expired",
                example = "true"
        )
        boolean isValid,

        @Schema(
                description = "User's email address associated with the token",
                example = "john.driver@example.com"
        )
        String email,

        @Schema(
                description = "User's role decoded from the token (DRIVER or PASSENGER)",
                example = "DRIVER"
        )
        String role) {
}
