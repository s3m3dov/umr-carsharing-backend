package com.cimhub.tripservice.service;

import com.cimhub.common.model.Points;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveRidesServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private BookingRepository bookingRepository;

    private TimezoneConversionService timezoneConversionService;
    private ActiveRidesService activeRidesService;

    @BeforeEach
    void setUp() {
        timezoneConversionService = new TimezoneConversionService();
        activeRidesService = new ActiveRidesService(tripsRepository, timezoneConversionService, bookingRepository);
    }

    @Test
    void findDriverActiveTrips_shouldMapSeatAndEarningFields() {
        Trip trip = buildTrip("trip-1", TripStatus.AVAILABLE, 4, 2, 9.5);

        when(tripsRepository.findByDriverIdAndTripStatusIn("driver-1",
                List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS)))
                .thenReturn(List.of(trip));

        List<DriverTripResponse> responses = activeRidesService.findDriverActiveTrips("driver-1");

        assertEquals(1, responses.size());
        DriverTripResponse response = responses.getFirst();
        assertEquals("trip-1", response.tripId());
        assertEquals(2, response.availableSeats());
        assertEquals(2, response.bookedSeats());
        assertEquals(19.0, response.estimatedEarnings());

        verify(tripsRepository).findByDriverIdAndTripStatusIn("driver-1",
                List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS));
    }

    @Test
    void findPassengerActiveRides_whenTripExists_shouldPopulateCarType() {
        Booking booking = buildBooking("booking-1", "trip-1", BookingStatus.CONFIRMED, "Europe/Berlin");
        Trip trip = buildTrip("trip-1", TripStatus.AVAILABLE, 4, 1, 12.0);

        when(bookingRepository.findByPassengerIdAndStatusIn("passenger-1",
                List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of(booking));
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        List<PassengerRideResponse> responses = activeRidesService.findPassengerActiveRides("passenger-1");

        assertEquals(1, responses.size());
        PassengerRideResponse response = responses.getFirst();
        assertEquals("booking-1", response.rideId());
        assertEquals("SEDAN", response.carType());
        assertEquals(2, response.bookedSeats());
    }

    @Test
    void findPassengerActiveRides_whenTripMissing_shouldSetCarTypeNull() {
        Booking booking = buildBooking("booking-2", "missing-trip", BookingStatus.REQUESTED, "UTC");

        when(bookingRepository.findByPassengerIdAndStatusIn("passenger-2",
                List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of(booking));
        when(tripsRepository.findById("missing-trip")).thenReturn(Optional.empty());

        List<PassengerRideResponse> responses = activeRidesService.findPassengerActiveRides("passenger-2");

        assertEquals(1, responses.size());
        assertEquals(null, responses.getFirst().carType());
    }

    @Test
    void findPassengerRideHistory_shouldUseHistoryStatuses() {
        when(bookingRepository.findByPassengerIdAndStatusIn("passenger-3",
                List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED)))
                .thenReturn(List.of());

        List<PassengerRideResponse> responses = activeRidesService.findPassengerRideHistory("passenger-3");

        assertTrue(responses.isEmpty());
        verify(bookingRepository).findByPassengerIdAndStatusIn("passenger-3",
                List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
    }

    @Test
    void findDriverTripHistory_shouldMapCompletedAndCancelledTrips() {
        Trip completed = buildTrip("trip-c", TripStatus.COMPLETED, 4, 4, 10.0);
        Trip cancelled = buildTrip("trip-x", TripStatus.CANCELLED, 4, 1, 10.0);

        when(tripsRepository.findByDriverIdAndTripStatusIn("driver-2",
                List.of(TripStatus.COMPLETED, TripStatus.CANCELLED)))
                .thenReturn(List.of(completed, cancelled));

        List<DriverTripResponse> responses = activeRidesService.findDriverTripHistory("driver-2");

        assertEquals(2, responses.size());
        assertEquals("trip-c", responses.getFirst().tripId());
        assertEquals(4, responses.getFirst().bookedSeats());
        assertEquals(40.0, responses.getFirst().estimatedEarnings());
    }

    private Trip buildTrip(String tripId, TripStatus status, int totalSeats, int bookedSeats, double pricePerSeat) {
        Trip trip = new Trip();
        trip.setTripId(tripId);
        trip.setDriverId("driver-1");
        trip.setVehicleNumber("HE-XX-1234");
        trip.setCarType("SEDAN");
        trip.setTripStatus(status);
        trip.setTotalSeats(totalSeats);
        trip.setBookedSeats(bookedSeats);
        trip.setPricePerSeat(pricePerSeat);
        trip.setTripTimezone("Europe/Berlin");
        trip.setTripStartDateTimeUTC(Instant.parse("2026-03-01T10:00:00Z"));
        trip.setRouteDistance(12000.0);
        trip.setRouteDuration(1800.0);
        trip.setSourceAddress(new Points());
        trip.setDestinationAddress(new Points());
        return trip;
    }

    private Booking buildBooking(String bookingId, String tripId, BookingStatus status, String timezone) {
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setTripId(tripId);
        booking.setDriverId("driver-1");
        booking.setPassengerId("passenger-1");
        booking.setVehicleNumber("HE-XX-1234");
        booking.setStatus(status);
        booking.setRequestedSeats(2);
        booking.setRideTimezone(timezone);
        booking.setRideStartTimeUTC(ZonedDateTime.of(2026, 3, 1, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant());
        booking.setPricePerSeat(12.5);
        booking.setEstimatedPrice(25.0);
        booking.setPickupLocation(new Points());
        booking.setDropoffLocation(new Points());
        return booking;
    }
}
