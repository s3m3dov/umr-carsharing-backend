package com.singhv.tripservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.singhv.common.models.Points;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for driver's upcoming trips view
 * Shows trips the driver is offering with passenger details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverUpcomingTripDTO {

    private String tripId;
    private String driverId;
    private String vehicleNumber;
    private String tripStatus;
    
    private Points sourceAddress;
    private Points destinationAddress;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime tripStartDateTime;
    
    private String tripTimezone;
    
    private int offeredSeat;
    private int availableSeats;  // offeredSeat - bookedSeats
    private int bookedSeats;     // currSeats
    
    // List of passengers who booked this trip
    private List<PassengerInfo> passengers;
    
    private Double routeDistanceInKm;
    private Double routeDurationInMinutes;
    private Double pricePerKm;
    private Double estimatedEarnings;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerInfo {
        private String userId;
        private int bookedSeats;
        private Points pickupLocation;
        private Points dropoffLocation;
    }
}

