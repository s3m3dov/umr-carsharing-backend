package com.cimhub.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreatePassengerRequest(
        @NotBlank(message = "Email is required") @Email String email,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        String phoneNumber,
        int age) {}
