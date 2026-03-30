package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import lombok.Builder;

import java.util.Date;

@Builder
public record DriverResponse(
        String userId,
        String firstName,
        String lastName,
        String email,
        UserType userType,
        String phoneNumber,
        int age,
        String licenseNumber,
        DriverStatus driverStatus,
        Date createdDate) {
}
