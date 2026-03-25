package com.cimhub.vehicleservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "VehicleResponse",
        description = "Vehicle information response",
        example = """
                {
                  "value": "vehicle_001",
                  "text": "Tesla Model 3 - HE-XX-1234",
                  "seatingCapacity": "5"
                }
                """
)
public record VehicleResponse(
        @Schema(description = "Vehicle's unique identifier", example = "vehicle_001")
        String value,

        @Schema(description = "Display text showing vehicle model and license plate", example = "Tesla Model 3 - HE-XX-1234")
        String text,

        @Schema(description = "Number of seats available for passengers", example = "5")
        String seatingCapacity) {
}
