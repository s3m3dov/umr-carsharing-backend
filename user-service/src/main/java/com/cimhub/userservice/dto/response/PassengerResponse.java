package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PassengerResponse(
        String userId,
        String firstName,
        String lastName,
        String email,
        UserType userType,
        String phoneNumber,
        int age,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt) {
}
