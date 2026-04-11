package com.cimhub.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
        name = "SignUpResponse",
        description = "Successful user registration response containing JWT token and user information",
        example = """
                {
                  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyIsInJvbGUiOiJEUklWRVIiLCJleHAiOjE2OTk4NzcyMDB9.signature",
                  "email": "john.driver@example.com",
                  "role": "DRIVER"
                }
                """)
public record SignUpResponse(
        @Schema(
                description = "JWT authentication token for subsequent API requests",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Registered user's email address", example = "john.driver@example.com")
        String email,

        @Schema(description = "User's role in the system (DRIVER or PASSENGER)", example = "DRIVER")
        String role) {}
