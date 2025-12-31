package com.cimhub.userservice.dto.response;

public record SignUpResponse(
        String userId,
        String username,
        boolean signUpSuccess) {
}
