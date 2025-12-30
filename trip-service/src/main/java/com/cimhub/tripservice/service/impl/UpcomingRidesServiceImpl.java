package com.cimhub.tripservice.service.impl;

import com.cimhub.tripservice.dto.DriverUpcomingTripDTO;
import com.cimhub.tripservice.dto.PassengerUpcomingRideDTO;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.TimezoneConversionService;
import com.cimhub.tripservice.service.UpcomingRidesService;
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
        private final BookingRepository bookingRepository;

        @Override
        public List<DriverUpcomingTripDTO> getDriverUpcomingTrips(String driverId) {
                log.info("Fetching upcoming trips for driver: {}", driverId);

                // Get all trips for this driver starting from now
                Instant now = Instant.now();
                List<Trip> upcomingTrips = tripsRepository.findByDriverIdAndTripStartDateTimeUTCBetween(
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
                List<Booking> upcomingBookings = bookingRepository.findByPassengerIdAndBookingStartTimeUTCBetween(
                                passengerId,
                                now,
                                now.plusSeconds(365L * 24 * 60 * 60) // Next year
                );

                log.info("Found {} upcoming bookings for passenger: {}", upcomingBookings.size(), passengerId);

                return upcomingBookings.stream()
                                .map(this::convertToPassengerUpcomingRideDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Convert Booking entity to PassengerUpcomingRideDTO
         */
        private PassengerUpcomingRideDTO convertToPassengerUpcomingRideDTO(Booking booking) {
                // Convert UTC time to original booking timezone
                ZonedDateTime bookingTimeInOriginalZone = timezoneService.convertToTimezone(
                                booking.getBookingStartTimeUTC(),
                                booking.getTimezone());

                return PassengerUpcomingRideDTO.builder()
                                .rideId(booking.getBookingId())
                                .tripId(booking.getTripId())
                                .driverId(booking.getDriverId())
                                .rideStatus(booking.getStatus().name())
                                .pickupLocation(booking.getPickupLocation())
                                .dropoffLocation(booking.getDropoffLocation())
                                .tripStartDateTime(bookingTimeInOriginalZone)
                                .tripTimezone(booking.getTimezone())
                                .bookedSeats(booking.getRequestedSeats())
                                .build();
        }

        /**
         * Convert Trips entity to DriverUpcomingTripDTO
         */
        private DriverUpcomingTripDTO convertToDriverUpcomingTripDTO(Trip trip) {
                // Convert UTC time to original trip timezone
                ZonedDateTime tripTimeInOriginalZone = timezoneService.convertToTripTimezone(
                                trip.getTripStartDateTimeUTC(),
                                trip.getTripTimezone());

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
