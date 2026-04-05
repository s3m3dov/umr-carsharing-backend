package com.cimhub.tripservice.dto.request;

import com.cimhub.common.models.Points;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(
        name = "FindRideRequest",
        description = "Request to search for available rides"
)
public record FindRideRequest(
        @Schema(description = "Passenger's pickup location")
        @NotNull(message = "Pickup location is required")
        Points pickupPoint,

        @Schema(description = "Passenger's drop-off location")
        @NotNull(message = "Drop-off location is required")
        Points destinationPoint,

        @Schema(description = "Desired ride start time in ISO-8601 format", example = "2024-03-25T08:00:00Z")
        String rideStartTime,

        @Schema(description = "Number of seats requested", example = "1")
        @Min(value = 1, message = "At least 1 seat must be requested")
        @Max(value = 8, message = "Maximum 8 seats allowed")
        Integer requestedSeats) {
}