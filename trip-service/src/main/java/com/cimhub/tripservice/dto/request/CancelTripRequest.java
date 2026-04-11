package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.dto.examples.TripApiDoc;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(
        name = "CancelTripRequest",
        description = "Request to cancel a trip or booking",
        example = TripApiDoc.CANCEL_TRIP_REQUEST
)
public record CancelTripRequest(
        @Schema(description = "Trip identifier to cancel", example = "trip_001")
        @NotBlank(message = "Trip ID is required")
        String tripId,

        @Schema(description = "Booking/ride identifier (only for passenger cancellations)", example = "ride_001")
        String rideId,

        @Schema(description = "Reason for cancellation", example = "Change of plans")
        String cancellationReason) {
}
