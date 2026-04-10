package com.cimhub.userservice.dto.request;

public record UpdateDriverRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        Integer age,
        String licenseNumber) implements UpdateProfileRequest {
}
