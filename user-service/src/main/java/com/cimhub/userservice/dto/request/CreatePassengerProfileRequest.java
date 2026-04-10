package com.cimhub.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePassengerProfileRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        String phoneNumber,
        int age) implements CreateProfileRequest {
}
