package com.cimhub.tripservice.dto.response;

import com.cimhub.tripservice.model.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(name = "StartTripResponse", description = "Response after successfully starting a trip")
public record StartTripResponse(
        @Schema(description = "Unique trip identifier", example = "trip_001")
        String tripId,

        @Schema(description = "Driver's user ID", example = "john.driver@example.com")
        String driverId,

        @Schema(description = "Current trip status", example = "IN_PROGRESS")
        TripStatus tripStatus,

        @Schema(description = "Timestamp when trip status was updated", example = "2026-04-06T09:30:00Z")
        Instant updatedAt) {
}
