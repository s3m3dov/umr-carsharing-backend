package com.singhv.tripservice.service;

import com.singhv.tripservice.dto.DriverUpcomingTripDTO;
import com.singhv.tripservice.dto.PassengerUpcomingRideDTO;

import java.util.List;

public interface UpcomingRidesService {
    
    /**
     * Get all upcoming trips for a driver
     * @param driverId Driver's user ID
     * @return List of upcoming trips the driver is offering
     */
    List<DriverUpcomingTripDTO> getDriverUpcomingTrips(String driverId);
    
    /**
     * Get all upcoming rides for a passenger
     * @param passengerId Passenger's user ID
     * @return List of upcoming rides the passenger has booked
     */
    List<PassengerUpcomingRideDTO> getPassengerUpcomingRides(String passengerId);
}

