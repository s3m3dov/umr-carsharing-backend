package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record TripResponse(
        String tripId,
        String driverId,
        String vehicleNumber,
        Points sourceAddress,
        Points destinationAddress,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTime,

        String tripTimezone,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTimeInUserTimezone,

        String userTimezone,
        int offeredSeat,
        int availableSeats,
        String tripStatus,
        double pricePerKm) {
}
