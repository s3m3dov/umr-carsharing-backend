package com.cimhub.tripservice.dto.request;

import lombok.Builder;

@Builder
public record CancelTripRequest(
        String userId,
        String tripId, // For both passenger and driver cancellations
        String rideId, // Only for passenger cancellations (bookingId)
        String cancellationReason) {
}
