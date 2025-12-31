package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.dto.common.LocationPoint;
import lombok.Builder;

@Builder
public record FindRideRequest(
        LocationPoint pickupPoint,
        LocationPoint destinationPoint,
        String rideStartTime,
        Integer requestedSeats) {
}
