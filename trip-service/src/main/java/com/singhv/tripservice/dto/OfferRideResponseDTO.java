package com.singhv.tripservice.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.singhv.common.models.Points;
import com.singhv.tripservice.dto.OSRM.Geometry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRideResponseDTO {

    private String tripId;
    private String vehicleNumber;
    private Points sourceAddress;
    private Points destinationAddress;

    // Return time with timezone information
    // Client can convert this to their local timezone for display
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    private ZonedDateTime tripStartDateTime;

    // Original timezone where the trip starts (e.g., "Asia/Kolkata")
    private String tripTimezone;

    // Route information from OSRM
    private Geometry routeGeometry; // Full route path as GeoJSON LineString
    private Double routeDistanceInMeters; // Distance in meters
    private Double routeDistanceInKm; // Distance in kilometers (for convenience)
    private Double routeDurationInSeconds; // Duration in seconds
    private Double routeDurationInMinutes; // Duration in minutes (for convenience)

    private Boolean tripCreated;
    private String errorMessage = null;
}
