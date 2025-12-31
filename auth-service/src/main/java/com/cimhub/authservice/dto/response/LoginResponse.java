package com.cimhub.authservice.dto.response;

public record LoginResponse(
        String token,
        String emailId) {
}
