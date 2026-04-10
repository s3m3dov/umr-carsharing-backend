package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.dto.examples.TripApiDoc;
import com.cimhub.common.models.Points;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.time.ZonedDateTime;

@Schema(
        name = "OfferRideRequest",
        description = "Request to create a new trip",
        example = TripApiDoc.OFFER_TRIP_REQUEST
)
@Builder
public record OfferRideRequest(
        @Schema(description = "Driver's user ID", example = "john.driver@example.com")
        String driverId,

        @Schema(description = "Trip starting location")
        Points sourceAddress,

        @Schema(description = "Trip destination location")
        Points destinationAddress,

        @Schema(example = "2026-01-11T13:09:26.095Z", description = "ISO-8601 date-time with timezone offset (e.g., 'Z' for UTC or '+01:00'). Milliseconds are optional.") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX") ZonedDateTime tripStartDateTime,

        @Schema(description = "Total number of seats available for passengers", example = "3")
        @Min(value = 1, message = "Total seats must be at least 1") int totalSeats,

        @Schema(description = "Price per seat in EUR", example = "5.0")
        @PositiveOrZero double pricePerSeat) {
}
