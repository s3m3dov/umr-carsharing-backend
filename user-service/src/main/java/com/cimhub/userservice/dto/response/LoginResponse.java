package com.cimhub.userservice.dto.response;

public record LoginResponse(
        String username,
        String userId,
        boolean loginSuccess,
        String errMsg) {
}
