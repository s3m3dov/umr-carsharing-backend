package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.UserType;

import java.util.Date;

public record UserResponse(
        String fullName,
        String email,
        String userId,
        UserType userType,
        String phoneNumber,
        int age,
        Date dob,
        String empId,
        String organisationName,
        String profilePicUrl) {
}
