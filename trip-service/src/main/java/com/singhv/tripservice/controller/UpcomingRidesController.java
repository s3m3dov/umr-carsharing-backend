package com.singhv.tripservice.controller;

import com.singhv.common.dto.ResponseDTO;
import com.singhv.tripservice.dto.DriverUpcomingTripDTO;
import com.singhv.tripservice.dto.PassengerUpcomingRideDTO;
import com.singhv.tripservice.service.UpcomingRidesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for viewing upcoming rides/trips
 * - Drivers can see their upcoming trips
 * - Passengers can see their upcoming rides
 */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class UpcomingRidesController {

    private final UpcomingRidesService upcomingRidesService;

    /**
     * Get upcoming trips for the authenticated driver
     * Shows all trips the driver is offering that haven't started yet
     * 
     * @param userId Driver's user ID from request header/JWT
     * @return List of upcoming trips with passenger details
     */
    @GetMapping("/my-trips/upcoming")
    public ResponseEntity<ResponseDTO<List<DriverUpcomingTripDTO>>> getMyUpcomingTrips(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Fetching upcoming trips for driver: {}", userId);
        
        List<DriverUpcomingTripDTO> upcomingTrips = upcomingRidesService.getDriverUpcomingTrips(userId);
        
        ResponseDTO<List<DriverUpcomingTripDTO>> response = new ResponseDTO<>();
        response.setSuccess(true);
        response.setResponseContent(upcomingTrips);
        
        log.info("Returning {} upcoming trips for driver: {}", upcomingTrips.size(), userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get upcoming rides for the authenticated passenger
     * Shows all rides the passenger has booked that haven't started yet
     * 
     * @param userId Passenger's user ID from request header/JWT
     * @return List of upcoming rides with driver details
     */
    @GetMapping("/my-rides/upcoming")
    public ResponseEntity<ResponseDTO<List<PassengerUpcomingRideDTO>>> getMyUpcomingRides(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Fetching upcoming rides for passenger: {}", userId);
        
        List<PassengerUpcomingRideDTO> upcomingRides = upcomingRidesService.getPassengerUpcomingRides(userId);
        
        ResponseDTO<List<PassengerUpcomingRideDTO>> response = new ResponseDTO<>();
        response.setSuccess(true);
        response.setResponseContent(upcomingRides);
        
        log.info("Returning {} upcoming rides for passenger: {}", upcomingRides.size(), userId);
        
        return ResponseEntity.ok(response);
    }
}

