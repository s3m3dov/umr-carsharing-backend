package com.singhv.tripservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.singhv.common.models.Points;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for passenger's upcoming rides view
 * Shows rides the passenger has booked
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerUpcomingRideDTO {

    private String rideId;
    private String tripId;
    private String driverId;
    private String vehicleNumber;
    private String rideStatus;
    
    private Points pickupLocation;
    private Points dropoffLocation;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime tripStartDateTime;
    
    private String tripTimezone;
    
    private int bookedSeats;
    
    private Double rideDistanceInKm;
    private Double rideDurationInMinutes;
    private Double pricePerKm;
    private Double estimatedFare;
    
    private DriverDetails driverDetails;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DriverDetails {
        private String name;
        private Double rating;
        private Integer totalTrips;
    }
}

