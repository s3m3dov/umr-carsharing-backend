package com.cimhub.authservice.dto.request;

import com.cimhub.common.enums.UserType;
import lombok.Builder;

@Builder
public record CreateProfileRequest(
        String userId,
        String email,
        String firstName,
        String lastName,
        UserType userType,
        String phoneNumber,
        int age,
        String licenseNumber) {
}
