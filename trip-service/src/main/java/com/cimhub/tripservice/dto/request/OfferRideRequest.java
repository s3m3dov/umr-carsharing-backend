package com.cimhub.tripservice.dto.request;

import com.cimhub.common.models.Points;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record OfferRideRequest(
        String driverId,
        String vehicleNumber,
        Points sourceAddress,
        Points destinationAddress,

        @Schema(example = "2026-01-11T13:09:26.095Z", description = "ISO-8601 date-time with timezone offset (e.g., 'Z' for UTC or '+01:00'). Milliseconds are optional.") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX") ZonedDateTime tripStartDateTime,

        @Min(value = 1, message = "Total seats must be at least 1") int totalSeats) {
}
