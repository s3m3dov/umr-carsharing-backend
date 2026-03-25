package com.cimhub.vehicleservice.dto.request;

import com.cimhub.vehicleservice.config.ApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "RegisterVehicleRequest",
        description = "Vehicle registration request containing vehicle details",
        example = ApiExamples.REGISTER_VEHICLE_REQUEST
)
public record RegisterVehicleRequest(
        @Schema(description = "Driver's user ID who owns the vehicle", example = "user_123")
        @NotBlank(message = "User ID is required") String userId,

        @Schema(description = "Vehicle make and model name", example = "Tesla Model 3")
        @NotBlank(message = "Vehicle name is required") String vehicleName,

        @Schema(description = "License plate number (unique)", example = "HE-XX-1234")
        @NotBlank(message = "Vehicle number is required") String vehicleNumber,

        @Schema(description = "Vehicle type/category", example = "sedan", allowableValues = {"sedan", "suv", "minivan", "hatchback"})
        @NotBlank(message = "Vehicle type is required") String vehicleType,

        @Schema(description = "Vehicle exterior color", example = "white")
        String vehicleColor,

        @Schema(description = "Number of seats available for passengers", example = "5")
        String seatingCapacity) {
}
