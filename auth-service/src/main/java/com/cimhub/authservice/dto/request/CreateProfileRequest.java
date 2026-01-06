package com.cimhub.authservice.dto.request;

import lombok.Builder;

import java.util.Date;

@Builder
public record CreateProfileRequest(
        String userId,
        String email,
        String fullName,
        String phoneNumber,
        int age,
        Date dob,
        String empId,
        String organisationName,
        String profilePicUrl) {
}
