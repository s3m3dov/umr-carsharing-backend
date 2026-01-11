package com.cimhub.authservice.dto.response;

public record ValidateTokenResponse(
        boolean isValid,
        String email) {
}
