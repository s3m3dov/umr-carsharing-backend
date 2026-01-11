package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpcomingRidesService {

        private final TripsRepository tripsRepository;
        private final TimezoneConversionService timezoneService;
        private final BookingRepository bookingRepository;

        public List<DriverTripResponse> findDriverUpcomingTrips(String driverId) {
                log.info("Fetching upcoming trips for driver: {}", driverId);

                Instant now = Instant.now();
                List<Trip> upcomingTrips = tripsRepository.findByDriverIdAndTripStartDateTimeUTCBetween(
                                driverId,
                                now,
                                now.plusSeconds(365L * 24 * 60 * 60) // Next year
                );

                return upcomingTrips.stream()
                                .map(this::convertToDriverTripResponse)
                                .collect(Collectors.toList());
        }

        public List<PassengerRideResponse> findPassengerUpcomingRides(String passengerId) {
                log.info("Fetching upcoming rides for passenger: {}", passengerId);

                Instant now = Instant.now();
                List<Booking> upcomingBookings = bookingRepository.findByPassengerIdAndBookingStartTimeUTCBetween(
                                passengerId,
                                now,
                                now.plusSeconds(365L * 24 * 60 * 60) // Next year
                );

                return upcomingBookings.stream()
                                .map(this::convertToPassengerRideResponse)
                                .collect(Collectors.toList());
        }

        private PassengerRideResponse convertToPassengerRideResponse(Booking booking) {
                ZonedDateTime bookingTimeInOriginalZone = timezoneService.convertToTimezone(
                                booking.getBookingStartTimeUTC(),
                                booking.getTimezone());

                return PassengerRideResponse.builder()
                                .rideId(booking.getBookingId())
                                .tripId(booking.getTripId())
                                .driverId(booking.getDriverId())
                                .rideStatus(booking.getStatus())
                                .pickupLocation(booking.getPickupLocation())
                                .dropoffLocation(booking.getDropoffLocation())
                                .tripStartDateTime(bookingTimeInOriginalZone)
                                .tripTimezone(booking.getTimezone())
                                .bookedSeats(booking.getRequestedSeats())
                                .build();
        }

        private DriverTripResponse convertToDriverTripResponse(Trip trip) {
                ZonedDateTime tripTimeInOriginalZone = timezoneService.convertToTripTimezone(
                                trip.getTripStartDateTimeUTC(),
                                trip.getTripTimezone());

                int bookedSeats = trip.getBookedSeats();
                int availableSeats = trip.getTotalSeats() - bookedSeats;
                double estimatedEarnings = (trip.getRouteDistance() / 1000.0) * trip.getPricePerKm();

                return DriverTripResponse.builder()
                                .tripId(trip.getTripId())
                                .driverId(trip.getDriverId())
                                .vehicleNumber(trip.getVehicleNumber())
                                .tripStatus(trip.getTripStatus())
                                .sourceAddress(trip.getSourceAddress())
                                .destinationAddress(trip.getDestinationAddress())
                                .tripStartDateTime(tripTimeInOriginalZone)
                                .tripTimezone(trip.getTripTimezone())
                                .totalSeats(trip.getTotalSeats())
                                .availableSeats(availableSeats)
                                .bookedSeats(bookedSeats)
                                .passengers(new ArrayList<>())
                                .routeDistanceInKm(trip.getRouteDistance() / 1000.0)
                                .routeDurationInMinutes(trip.getRouteDuration() / 60.0)
                                .pricePerKm(trip.getPricePerKm())
                                .estimatedEarnings(estimatedEarnings)
                                .build();
        }
}
