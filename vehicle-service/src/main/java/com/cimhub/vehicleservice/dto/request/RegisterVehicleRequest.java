package com.cimhub.vehicleservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterVehicleRequest(
        @NotBlank(message = "User ID is required") String userId,

        @NotBlank(message = "Vehicle name is required") String vehicleName,

        @NotBlank(message = "Vehicle number is required") String vehicleNumber,

        @NotBlank(message = "Vehicle type is required") String vehicleType,

        String vehicleColor,
        String seatingCapacity) {
}
