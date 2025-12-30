package com.singhv.tripservice.service.impl;

import com.singhv.tripservice.dto.TripViewDTO;
import com.singhv.tripservice.model.Trips;
import com.singhv.tripservice.repository.TripsRepository;
import com.singhv.tripservice.service.TimezoneConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Example service showing how to retrieve trips with proper timezone handling
 */
@Service
@RequiredArgsConstructor
public class TripSearchServiceImpl {

    private final TripsRepository tripsRepository;
    private final TimezoneConversionService timezoneService;

    /**
     * Search for available trips and return with timezone conversion
     *
     * @param userTimezone The timezone of the user viewing trips (e.g., "Europe/Berlin")
     * @return List of trips with times converted to user's timezone
     */
    public List<TripViewDTO> searchAvailableTrips(String userTimezone) {
        List<Trips> allTrips = tripsRepository.findAll();

        return allTrips.stream()
                .filter(trip -> "OFFERED".equals(trip.getTripStatus()))
                .map(trip -> convertToViewDTO(trip, userTimezone))
                .collect(Collectors.toList());
    }

    /**
     * Convert Trip entity to TripViewDTO with timezone conversion
     */
    private TripViewDTO convertToViewDTO(Trips trip, String userTimezone) {
        // Convert UTC time to original trip timezone
        ZonedDateTime tripTimeInOriginalZone = timezoneService.convertToTripTimezone(
                trip.getTripStartDateTimeUTC(),
                trip.getTripTimezone()
        );

        // Convert UTC time to user's timezone for convenience
        ZonedDateTime tripTimeInUserZone = timezoneService.convertToUserTimezone(
                trip.getTripStartDateTimeUTC(),
                userTimezone
        );

        return TripViewDTO.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .sourceAddress(trip.getSourceAddress())
                .destinationAddress(trip.getDestinationAddress())
                .tripStartDateTime(tripTimeInOriginalZone)
                .tripTimezone(trip.getTripTimezone())
                .tripStartDateTimeInUserTimezone(tripTimeInUserZone)
                .userTimezone(userTimezone)
                .offeredSeat(trip.getOfferedSeat())
                .availableSeats(trip.getOfferedSeat() - trip.getCurrSeats())
                .tripStatus(trip.getTripStatus())
                .pricePerKm(trip.getPricePerKm())
                .build();
    }
}

