package com.cimhub.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDriverProfileRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        String phoneNumber,
        int age,
        @NotBlank(message = "License number is required") String licenseNumber)
        implements CreateProfileRequest {}
