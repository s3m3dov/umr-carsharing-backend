package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.TripStatus;
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
        Instant createdDate) {
}
