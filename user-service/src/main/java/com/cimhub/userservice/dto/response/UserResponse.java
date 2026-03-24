package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.UserType;

public record UserResponse(
        String userId,
        String firstName,
        String lastName,
        String email,
        UserType userType,
        String phoneNumber,
        int age,
        String vehicleModel,
        String licensePlate) {
}
