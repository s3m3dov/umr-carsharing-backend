package com.cimhub.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record RegisterUserRequest(
        @NotBlank(message = "Full name is required") String fullName,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Password is required") String password,

        String phoneNumber,
        int age,
        Date dob,
        String empId,
        String organisationName,
        String profilePicUrl) {
}
