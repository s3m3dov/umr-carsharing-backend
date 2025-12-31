package com.cimhub.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;

public record CreateProfileRequest(
        @NotBlank(message = "User ID is required") String userId,

        @NotBlank(message = "Email is required") String emailId,

        String fullName,
        String phoneNumber,
        int age,
        Date dob,
        String empId,
        String organisationName,
        String profilePicUrl) {
}
