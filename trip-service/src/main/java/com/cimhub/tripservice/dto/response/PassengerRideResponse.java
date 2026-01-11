package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record PassengerRideResponse(
        String rideId,
        String tripId,
        String driverId,
        String vehicleNumber,
        BookingStatus rideStatus,
        Points pickupLocation,
        Points dropoffLocation,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTime,

        String tripTimezone,
        int bookedSeats,
        Double rideDistanceInKm,
        Double rideDurationInMinutes,
        Double pricePerKm,
        Double estimatedFare,
        DriverDetails driverDetails) {
    @Builder
    public record DriverDetails(
            String name,
            Double rating,
            Integer totalTrips) {
    }
}
