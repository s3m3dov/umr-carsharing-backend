package com.cimhub.tripservice.dto.response;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;

@Builder
@Schema(name = "CompleteRideResponse", description = "Response after successfully completing a passenger ride")
public record CompleteRideResponse(
        @Schema(description = "Unique booking identifier", example = "booking_001")
        String bookingId,

        @Schema(description = "Associated trip identifier", example = "trip_001")
        String tripId,

        @Schema(description = "Driver's user ID", example = "john.driver@example.com")
        String driverId,

        @Schema(description = "Passenger's user ID", example = "jane.passenger@example.com")
        String passengerId,

        @Schema(description = "Current booking status", example = "COMPLETED")
        BookingStatus bookingStatus,

        @Schema(description = "Current trip status", example = "IN_PROGRESS")
        TripStatus tripStatus,

        @Schema(description = "Timestamp when ride status was updated", example = "2026-04-06T09:35:00Z")
        Instant updatedAt) {}
