package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.controller.BookingApiExamples;
import com.cimhub.common.models.Points;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(
        name = "JoinTripRequest",
        description = "Request to join an existing trip as a passenger",
        example = BookingApiExamples.JOIN_TRIP_REQUEST
)
public record JoinTripRequest(
        @Schema(description = "Trip identifier to join", example = "trip_001")
        @NotNull(message = "Trip ID is required")
        String tripId,

        @Schema(description = "Passenger's user ID", example = "jane.passenger@example.com")
        @NotNull(message = "Passenger ID is required")
        String passengerId,

        @Schema(description = "Driver's user ID for the trip", example = "john.driver@example.com")
        @NotNull(message = "Driver ID is required")
        String driverId,

        @Schema(description = "Passenger's pickup location")
        @NotNull(message = "Pickup location is required")
        Points pickupPoint,

        @Schema(description = "Passenger's drop-off location")
        @NotNull(message = "Drop-off location is required")
        Points destinationPoint,

        @Schema(description = "Desired ride start time in ISO-8601 format", example = "2024-03-25T08:00:00Z")
        String rideStartTime,

        @Schema(description = "Number of seats requested", example = "1")
        @NotNull(message = "Requested seats cannot be null")
        @Min(value = 1, message = "At least 1 seat must be requested")
        Integer requestedSeats) {
}
