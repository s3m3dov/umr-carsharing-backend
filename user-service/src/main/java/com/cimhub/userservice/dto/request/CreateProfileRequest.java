package com.cimhub.userservice.dto.request;

import com.cimhub.common.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record CreateProfileRequest(
                @NotBlank(message = "User ID is required") String userId,

                @NotBlank(message = "Email is required") String email,

                @NotNull(message = "UserType is required") UserType userType,

                String firstName,
                String lastName,
                String phoneNumber,
                int age,
                String vehicleModel,
                String licensePlate) {
}
