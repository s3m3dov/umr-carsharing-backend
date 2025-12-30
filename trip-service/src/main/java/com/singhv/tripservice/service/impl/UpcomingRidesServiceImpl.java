package com.singhv.tripservice.service.impl;

import com.singhv.tripservice.dto.DriverUpcomingTripDTO;
import com.singhv.tripservice.dto.PassengerUpcomingRideDTO;
import com.singhv.tripservice.model.Trips;
import com.singhv.tripservice.model.Rides;
import com.singhv.tripservice.repository.RidesRepository;
import com.singhv.tripservice.repository.TripsRepository;
import com.singhv.tripservice.service.TimezoneConversionService;
import com.singhv.tripservice.service.UpcomingRidesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpcomingRidesServiceImpl implements UpcomingRidesService {

    private final TripsRepository tripsRepository;
    private final TimezoneConversionService timezoneService;
    private final RidesRepository rideRepository;

    @Override
    public List<DriverUpcomingTripDTO> getDriverUpcomingTrips(String driverId) {
        log.info("Fetching upcoming trips for driver: {}", driverId);
        
        // Get all trips for this driver starting from now
        Instant now = Instant.now();
        List<Trips> upcomingTrips = tripsRepository.findByDriverIdAndTripStartDateTimeUTCBetween(
                driverId,
                now,
                now.plusSeconds(365L * 24 * 60 * 60) // Next year
        );
        
        log.info("Found {} upcoming trips for driver: {}", upcomingTrips.size(), driverId);
        
        return upcomingTrips.stream()
                .map(this::convertToDriverUpcomingTripDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PassengerUpcomingRideDTO> getPassengerUpcomingRides(String passengerId) {
        log.info("Fetching upcoming rides for passenger: {}", passengerId);
        
        Instant now = Instant.now();
        List<Rides> upcomingRides = rideRepository.findByPassengerIdAndRideStartTimeUTCBetween(
                passengerId,
                now,
                now.plusSeconds(365L * 24 * 60 * 60) // Next year
        );
        
        log.info("Found {} upcoming rides for passenger: {}", upcomingRides.size(), passengerId);
        
        return upcomingRides.stream()
                .map(this::convertToPassengerUpcomingRideDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Rides entity to PassengerUpcomingRideDTO
     */
    private PassengerUpcomingRideDTO convertToPassengerUpcomingRideDTO(Rides ride) {
        // Convert UTC time to original ride timezone
        ZonedDateTime rideTimeInOriginalZone = timezoneService.convertToTimezone(
                ride.getRideStartTimeUTC(),
                ride.getRideTimezone()
        );
        
        return PassengerUpcomingRideDTO.builder()
                .rideId(ride.getRideId())
                .tripId(ride.getTripId())
                .driverId(ride.getDriverId())
                .rideStatus(ride.getRideStatus())
                .pickupLocation(ride.getPickupLocation())
                .dropoffLocation(ride.getDropoffLocation())
                .tripStartDateTime(rideTimeInOriginalZone)
                .tripTimezone(ride.getRideTimezone())
                .bookedSeats(ride.getRequestedSeats())
                .build();
    }

    /**
     * Convert Trips entity to DriverUpcomingTripDTO
     */
    private DriverUpcomingTripDTO convertToDriverUpcomingTripDTO(Trips trip) {
        // Convert UTC time to original trip timezone
        ZonedDateTime tripTimeInOriginalZone = timezoneService.convertToTripTimezone(
                trip.getTripStartDateTimeUTC(),
                trip.getTripTimezone()
        );
        
        int bookedSeats = trip.getCurrSeats();
        int availableSeats = trip.getOfferedSeat() - bookedSeats;
        
        // Calculate estimated earnings
        double estimatedEarnings = (trip.getRouteDistance() / 1000.0) * trip.getPricePerKm();
        
        return DriverUpcomingTripDTO.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .tripStatus(trip.getTripStatus())
                .sourceAddress(trip.getSourceAddress())
                .destinationAddress(trip.getDestinationAddress())
                .tripStartDateTime(tripTimeInOriginalZone)
                .tripTimezone(trip.getTripTimezone())
                .offeredSeat(trip.getOfferedSeat())
                .availableSeats(availableSeats)
                .bookedSeats(bookedSeats)
                .passengers(new ArrayList<>()) // TODO: 
                .routeDistanceInKm(trip.getRouteDistance() / 1000.0)
                .routeDurationInMinutes(trip.getRouteDuration() / 60.0)
                .pricePerKm(trip.getPricePerKm())
                .estimatedEarnings(estimatedEarnings)
                .build();
    }
}

