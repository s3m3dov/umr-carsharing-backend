package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.BookingStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record BookingResponse(
        String bookingId,
        String passengerId,
        String tripId,
        String driverId,
        String vehicleNumber,
        Points pickupLocation,
        Points dropoffLocation,
        int requestedSeats,
        BookingStatus status,
        double estimatedPrice,
        double pricePerSeat,
        Instant rideStartTimeUTC,
        String rideTimezone,
        Instant createdAt,
        Instant updatedAt) {
}
