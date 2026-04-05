package com.cimhub.tripservice.dto.response;

import com.cimhub.tripservice.model.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(name = "CompleteTripResponse", description = "Response after successfully completing a trip")
public record CompleteTripResponse(
        @Schema(description = "Unique trip identifier", example = "trip_001")
        String tripId,

        @Schema(description = "Driver's user ID", example = "john.driver@example.com")
        String driverId,

        @Schema(description = "Current trip status", example = "COMPLETED")
        TripStatus tripStatus,

        @Schema(description = "Number of active rides auto-completed during trip completion", example = "2")
        int autoCompletedRides,

        @Schema(description = "Timestamp when trip status was updated", example = "2026-04-06T09:50:00Z")
        Instant updatedAt) {
}
