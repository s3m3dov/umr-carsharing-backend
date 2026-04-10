package com.cimhub.tripservice.dto.response;

import com.cimhub.common.model.Points;
import com.cimhub.tripservice.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(
        name = "BookingResponse",
        description = "Booking details response"
)
public record BookingResponse(
        @Schema(description = "Unique booking identifier", example = "booking_001")
        String bookingId,

        @Schema(description = "Passenger's user ID", example = "jane.passenger@example.com")
        String passengerId,

        @Schema(description = "Associated trip identifier", example = "trip_001")
        String tripId,

        @Schema(description = "Driver's user ID", example = "john.driver@example.com")
        String driverId,

        @Schema(description = "Vehicle license plate number", example = "HE-XX-1234")
        String vehicleNumber,

        @Schema(description = "Passenger's pickup location")
        Points pickupLocation,

        @Schema(description = "Passenger's drop-off location")
        Points dropoffLocation,

        @Schema(description = "Number of seats requested", example = "1")
        int requestedSeats,

        @Schema(description = "Current booking status", example = "CONFIRMED")
        BookingStatus status,

        @Schema(description = "Estimated total price in EUR", example = "15.50")
        double estimatedPrice,

        @Schema(description = "Price per seat in EUR", example = "15.50")
        double pricePerSeat,

        @Schema(description = "Ride start time in UTC", example = "2026-04-05T10:00:00Z")
        Instant rideStartTimeUTC,

        @Schema(description = "Timezone of the ride start", example = "Europe/Berlin")
        String rideTimezone,

        @Schema(description = "Timestamp when booking was created", example = "2026-04-04T12:00:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when booking was last updated", example = "2026-04-04T14:30:00Z")
        Instant updatedAt) {
}