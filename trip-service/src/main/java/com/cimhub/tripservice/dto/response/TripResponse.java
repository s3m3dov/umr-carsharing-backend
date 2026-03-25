package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
@Schema(name = "TripResponse", description = "Trip details as seen in search results")
public record TripResponse(
        @Schema(description = "Unique trip identifier", example = "trip_001")
        String tripId,

        @Schema(description = "Driver's user ID", example = "user_123")
        String driverId,

        @Schema(description = "Vehicle license plate number", example = "HE-XX-1234")
        String vehicleNumber,

        @Schema(description = "Trip starting location")
        Points sourceAddress,

        @Schema(description = "Trip destination location")
        Points destinationAddress,

        @Schema(description = "Trip departure date and time in UTC", example = "2026-01-11T13:09:26Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime tripStartDateTime,

        @Schema(description = "Timezone of the trip departure", example = "Europe/Berlin")
        String tripTimezone,

        @Schema(description = "Trip departure time converted to user's timezone", example = "2026-01-11T14:09:26+01:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime tripStartDateTimeInUserTimezone,

        @Schema(description = "Timezone of the searching user", example = "Europe/Berlin")
        String userTimezone,

        @Schema(description = "Total seats in the vehicle", example = "4")
        int totalSeats,

        @Schema(description = "Number of seats still available", example = "3")
        int availableSeats,

        @Schema(description = "Current trip status", example = "OFFERED")
        TripStatus tripStatus,

        @Schema(description = "Price per kilometer in EUR", example = "0.15")
        double pricePerKm) {
}
