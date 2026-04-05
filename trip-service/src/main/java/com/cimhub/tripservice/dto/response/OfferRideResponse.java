package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.dto.OSRM.Geometry;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
@Schema(name = "OfferRideResponse", description = "Response after successfully creating a trip")
public record OfferRideResponse(
        @Schema(description = "Unique trip identifier", example = "trip_001")
        String tripId,

        @Schema(description = "Vehicle license plate number", example = "HE-XX-1234")
        String vehicleNumber,

        @Schema(description = "Trip starting location")
        Points sourceAddress,

        @Schema(description = "Trip destination location")
        Points destinationAddress,

        @Schema(description = "Trip departure date and time", example = "2026-01-11T13:09:26+01:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime tripStartDateTime,

        @Schema(description = "Timezone of the trip departure", example = "Europe/Berlin")
        String tripTimezone,

        @Schema(description = "Route geometry as GeoJSON object")
        Geometry routeGeometry,

        @Schema(description = "Total route distance in meters", example = "125430.5")
        Double routeDistanceInMeters,

        @Schema(description = "Total route distance in kilometers", example = "125.4")
        Double routeDistanceInKm,

        @Schema(description = "Estimated route duration in seconds", example = "5400.0")
        Double routeDurationInSeconds,

        @Schema(description = "Estimated route duration in minutes", example = "90.0")
        Double routeDurationInMinutes,

        @Schema(description = "Price per seat in EUR", example = "5.0")
        Double pricePerSeat,

        @Schema(description = "Whether the trip was successfully created", example = "true")
        Boolean tripCreated,

        @Schema(description = "Error message if trip creation failed", example = "null")
        String errorMessage) {
}
