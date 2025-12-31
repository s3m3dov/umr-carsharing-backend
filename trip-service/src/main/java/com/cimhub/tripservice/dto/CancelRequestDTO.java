package com.cimhub.tripservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for cancellation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequestDTO {

    private String userId; // TODO: Extract from JWT in future
    private String tripId; // For both passenger and driver cancellations
    private String rideId; // Only for passenger cancellations (bookingId)
    private String cancellationReason;
}
