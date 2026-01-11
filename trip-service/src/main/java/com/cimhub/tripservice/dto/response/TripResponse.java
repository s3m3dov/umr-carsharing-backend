package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record TripResponse(
        String tripId,
        String driverId,
        String vehicleNumber,
        Points sourceAddress,
        Points destinationAddress,

        @Schema(example = "2026-01-11T13:09:26Z") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTime,

        String tripTimezone,

        @Schema(example = "2026-01-11T14:09:26+01:00") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTimeInUserTimezone,

        String userTimezone,
        int totalSeats,
        int availableSeats,
        TripStatus tripStatus,
        double pricePerKm) {
}
