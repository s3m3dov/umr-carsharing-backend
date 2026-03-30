package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;

@Builder
public record AdminTripResponse(
        String tripId,
        String driverId,
        String vehicleNumber,
        String carType,
        TripStatus tripStatus,
        Points sourceAddress,
        Points destinationAddress,
        int totalSeats,
        int bookedSeats,
        int availableSeats,
        double pricePerSeat,
        double routeDistance,
        double routeDuration,
        Instant tripStartDateTimeUTC,
        String tripTimezone,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt) {
}
