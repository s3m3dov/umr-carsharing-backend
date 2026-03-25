package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.config.ApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(
        name = "CancelTripRequest",
        description = "Request to cancel a trip or booking",
        example = ApiExamples.CANCEL_TRIP_REQUEST
)
@Builder
public record CancelTripRequest(
        String userId,
        String tripId, // For both passenger and driver cancellations
        String rideId, // Only for passenger cancellations (bookingId)
        String cancellationReason) {
}
