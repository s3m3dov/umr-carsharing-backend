package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.dto.examples.TripApiDoc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(
        name = "CancelTripRequest",
        description = "Request to cancel a trip or booking",
        example = TripApiDoc.CANCEL_TRIP_REQUEST
)
@Builder
public record CancelTripRequest(
        @Schema(description = "User ID of the person cancelling", example = "john.driver@example.com")
        String userId,

        @Schema(description = "Trip identifier to cancel", example = "trip_001")
        String tripId,

        @Schema(description = "Booking/ride identifier (only for passenger cancellations)", example = "ride_001")
        String rideId,

        @Schema(description = "Reason for cancellation", example = "Change of plans")
        String cancellationReason) {
}
