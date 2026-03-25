package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Schema(name = "DriverTripResponse", description = "Trip details as seen by the driver, including passenger information")
public record DriverTripResponse(
        @Schema(description = "Unique trip identifier", example = "trip_001")
        String tripId,

        @Schema(description = "Driver's user ID", example = "user_123")
        String driverId,

        @Schema(description = "Vehicle license plate number", example = "HE-XX-1234")
        String vehicleNumber,

        @Schema(description = "Current trip status", example = "OFFERED")
        TripStatus tripStatus,

        @Schema(description = "Trip starting location")
        Points sourceAddress,

        @Schema(description = "Trip destination location")
        Points destinationAddress,

        @Schema(description = "Trip departure date and time", example = "2026-01-11T13:09:26Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime tripStartDateTime,

        @Schema(description = "Timezone of the trip departure", example = "Europe/Berlin")
        String tripTimezone,

        @Schema(description = "Total seats in the vehicle", example = "4")
        int totalSeats,

        @Schema(description = "Number of seats still available", example = "3")
        int availableSeats,

        @Schema(description = "Number of seats currently booked", example = "1")
        int bookedSeats,

        @Schema(description = "List of passengers who booked this trip")
        List<PassengerInfo> passengers,

        @Schema(description = "Total route distance in kilometers", example = "125.4")
        Double routeDistanceInKm,

        @Schema(description = "Estimated route duration in minutes", example = "90.0")
        Double routeDurationInMinutes,

        @Schema(description = "Price per seat in EUR", example = "15.50")
        Double pricePerSeat,

        @Schema(description = "Estimated total earnings for this trip in EUR", example = "18.81")
        Double estimatedEarnings) {

    @Builder
    @Schema(name = "PassengerInfo", description = "Information about a passenger booking on this trip")
    public record PassengerInfo(
            @Schema(description = "Passenger's user ID", example = "user_456")
            String userId,

            @Schema(description = "Number of seats booked by this passenger", example = "1")
            int bookedSeats,

            @Schema(description = "Passenger's pickup location")
            Points pickupLocation,

            @Schema(description = "Passenger's drop-off location")
            Points dropoffLocation) {
    }
}
