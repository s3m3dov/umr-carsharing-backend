package com.singhv.tripservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for finding available rides
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindRideRequestDTO {

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
