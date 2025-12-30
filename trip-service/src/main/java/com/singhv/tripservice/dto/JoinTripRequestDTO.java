package com.singhv.tripservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for passenger joining a trip
 * Matches the frontend request structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinTripRequestDTO {

    private String tripId;
    private String driverId;
    private LocationPoint pickupPoint;
    private LocationPoint destinationPoint;
    private String rideStartTime;
    private Integer requestedSeats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationPoint {
        private Double latitude;
        private Double longitude;
        private String placeAddress;
    }
}
