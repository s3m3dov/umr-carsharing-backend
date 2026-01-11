package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record DriverTripResponse(
        String tripId,
        String driverId,
        String vehicleNumber,
        TripStatus tripStatus,
        Points sourceAddress,
        Points destinationAddress,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTime,

        String tripTimezone,
        int totalSeats,
        int availableSeats,
        int bookedSeats,
        List<PassengerInfo> passengers,
        Double routeDistanceInKm,
        Double routeDurationInMinutes,
        Double pricePerKm,
        Double estimatedEarnings) {
    @Builder
    public record PassengerInfo(
            String userId,
            int bookedSeats,
            Points pickupLocation,
            Points dropoffLocation) {
    }
}
