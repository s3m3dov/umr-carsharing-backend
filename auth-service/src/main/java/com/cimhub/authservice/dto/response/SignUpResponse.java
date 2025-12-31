package com.cimhub.authservice.dto.response;

import lombok.Builder;

@Builder
public record SignUpResponse(
        String token,
        String emailId) {
}
