package com.cimhub.userservice.dto.response;

import java.util.Date;

public record UserResponse(
        String fullName,
        String emailId,
        String userId,
        String phoneNumber,
        int age,
        Date dob,
        String empId,
        String organisationName,
        String profilePicUrl) {
}
