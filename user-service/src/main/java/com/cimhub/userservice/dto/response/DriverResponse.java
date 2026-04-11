package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.Builder;

@Builder
public record DriverResponse(
        String userId,
        String firstName,
        String lastName,
        @JsonInclude(JsonInclude.Include.NON_NULL) String email,
        UserType userType,
        String phoneNumber,
        int age,
        String licenseNumber,
        DriverStatus driverStatus,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt) {}
