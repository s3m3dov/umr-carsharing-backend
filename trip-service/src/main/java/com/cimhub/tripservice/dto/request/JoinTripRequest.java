package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.dto.common.LocationPoint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record JoinTripRequest(
                String tripId,
                String passengerId,
                String driverId,
                LocationPoint pickupPoint,
                LocationPoint destinationPoint,
                String rideStartTime,
                @NotNull(message = "Requested seats cannot be null") @Min(value = 1, message = "At least 1 seat must be requested") Integer requestedSeats) {
}
