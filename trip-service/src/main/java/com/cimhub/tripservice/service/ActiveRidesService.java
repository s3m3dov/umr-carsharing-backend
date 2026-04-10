package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveRidesService {

        private final TripsRepository tripsRepository;
        private final TimezoneConversionService timezoneService;
        private final BookingRepository bookingRepository;
        private final TripMapper tripMapper;

        public List<DriverTripResponse> findDriverActiveTrips(String driverId) {
                log.info("Fetching active trips for driver: {}", driverId);

                List<TripStatus> activeStatuses = List.of(
                                TripStatus.CREATED,
                                TripStatus.AVAILABLE,
                                TripStatus.IN_PROGRESS);

                List<Trip> activeTrips = tripsRepository.findByDriverIdAndTripStatusIn(driverId, activeStatuses);

                return activeTrips.stream()
                                .map(tripMapper::toDriverTripResponse)
                                .collect(Collectors.toList());
        }

        public List<DriverTripResponse> findDriverTripHistory(String driverId) {
                log.info("Fetching trip history for driver: {}", driverId);

                List<TripStatus> historyStatuses = List.of(
                                TripStatus.COMPLETED,
                                TripStatus.CANCELLED);

                List<Trip> pastTrips = tripsRepository.findByDriverIdAndTripStatusIn(driverId, historyStatuses);

                return pastTrips.stream()
                                .map(tripMapper::toDriverTripResponse)
                                .collect(Collectors.toList());
        }

        public List<PassengerRideResponse> findPassengerActiveRides(String passengerId) {
                log.info("Fetching active rides for passenger: {}", passengerId);

                List<BookingStatus> activeStatuses = List.of(
                                BookingStatus.REQUESTED,
                                BookingStatus.CONFIRMED,
                                BookingStatus.IN_PROGRESS);

                List<Booking> activeBookings = bookingRepository.findByPassengerIdAndStatusIn(passengerId,
                                activeStatuses);

                return buildPassengerRideResponses(activeBookings);
        }

        public List<PassengerRideResponse> findPassengerRideHistory(String passengerId) {
                log.info("Fetching ride history for passenger: {}", passengerId);

                List<BookingStatus> historyStatuses = List.of(
                                BookingStatus.COMPLETED,
                                BookingStatus.CANCELLED);

                List<Booking> pastBookings = bookingRepository.findByPassengerIdAndStatusIn(passengerId,
                                historyStatuses);

                return buildPassengerRideResponses(pastBookings);
        }

        private List<PassengerRideResponse> buildPassengerRideResponses(List<Booking> bookings) {
                if (bookings.isEmpty()) {
                        return List.of();
                }

                List<String> tripIds = bookings.stream()
                                .map(Booking::getTripId)
                                .distinct()
                                .toList();

                Map<String, Trip> tripMap = tripsRepository.findAllById(tripIds).stream()
                                .collect(Collectors.toMap(Trip::getTripId, Function.identity()));

                return bookings.stream()
                                .map(booking -> convertToPassengerRideResponse(booking, tripMap.get(booking.getTripId())))
                                .collect(Collectors.toList());
        }

        private PassengerRideResponse convertToPassengerRideResponse(Booking booking) {
                Trip trip = tripsRepository.findById(booking.getTripId()).orElse(null);
                return convertToPassengerRideResponse(booking, trip);
        }

        private PassengerRideResponse convertToPassengerRideResponse(Booking booking, Trip trip) {
                ZonedDateTime bookingTimeInOriginalZone = timezoneService.convertToTimezone(
                                booking.getRideStartTimeUTC(),
                                booking.getRideTimezone());

                String carType = trip != null ? trip.getCarType() : null;

                return PassengerRideResponse.builder()
                                .rideId(booking.getBookingId())
                                .tripId(booking.getTripId())
                                .driverId(booking.getDriverId())
                                .vehicleNumber(booking.getVehicleNumber())
                                .carType(carType)
                                .rideStatus(booking.getStatus())
                                .pickupLocation(booking.getPickupLocation())
                                .dropoffLocation(booking.getDropoffLocation())
                                .tripStartDateTime(bookingTimeInOriginalZone)
                                .tripTimezone(booking.getRideTimezone())
                                .bookedSeats(booking.getRequestedSeats())
                                .pricePerSeat(booking.getPricePerSeat())
                                .estimatedFare(booking.getEstimatedPrice())
                                .build();
        }

}
