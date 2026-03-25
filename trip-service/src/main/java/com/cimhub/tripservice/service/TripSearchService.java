package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripSearchService {

        private final TripsRepository tripsRepository;
        private final TimezoneConversionService timezoneService;

        public List<TripResponse> searchAvailableTrips(String userTimezone) {
                List<Trip> allTrips = tripsRepository.findAll();

                return allTrips.stream()
                                .filter(trip -> trip.getTripStatus() == TripStatus.OFFERED
                                                || trip.getTripStatus() == TripStatus.AVAILABLE)
                                .map(trip -> convertToResponse(trip, userTimezone))
                                .collect(Collectors.toList());
        }

        private TripResponse convertToResponse(Trip trip, String userTimezone) {
                ZonedDateTime tripTimeInOriginalZone = timezoneService.convertToTripTimezone(
                                trip.getTripStartDateTimeUTC(),
                                trip.getTripTimezone());

                ZonedDateTime tripTimeInUserZone = timezoneService.convertToUserTimezone(
                                trip.getTripStartDateTimeUTC(),
                                userTimezone);

                return TripResponse.builder()
                                .tripId(trip.getTripId())
                                .driverId(trip.getDriverId())
                                .vehicleNumber(trip.getVehicleNumber())
                                .sourceAddress(trip.getSourceAddress())
                                .destinationAddress(trip.getDestinationAddress())
                                .tripStartDateTime(tripTimeInOriginalZone)
                                .tripTimezone(trip.getTripTimezone())
                                .tripStartDateTimeInUserTimezone(tripTimeInUserZone)
                                .userTimezone(userTimezone)
                                .totalSeats(trip.getTotalSeats())
                                .availableSeats(trip.getTotalSeats() - trip.getBookedSeats())
                                .tripStatus(trip.getTripStatus())
                                .pricePerSeat(trip.getPricePerSeat())
                                .build();
        }
}
