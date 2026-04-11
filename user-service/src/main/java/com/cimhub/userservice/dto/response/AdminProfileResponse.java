package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminProfileResponse", description = "Admin profile details derived from authenticated headers")
public record AdminProfileResponse(
        @Schema(description = "Admin email address", example = "admin@example.com")
        String adminEmail,
        @Schema(description = "Authenticated user role", example = "ADMIN")
        UserType role) {
}
