package com.cimhub.tripservice.service.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.common.testsupport.Points;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.mapper.BookingMapper;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Passenger;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.PassengerRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.util.TimezoneConversionService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActiveRidesServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    private ActiveRidesService activeRidesService;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private BookingMapper bookingMapper;

    private TimezoneConversionService timezoneConversionService;

    @BeforeEach
    void setUp() {
        timezoneConversionService = new TimezoneConversionService();
        tripMapper = new TripMapper(timezoneConversionService);
        bookingMapper = new BookingMapper(timezoneConversionService);
        activeRidesService = new ActiveRidesService(
                tripsRepository, bookingRepository, passengerRepository, tripMapper, bookingMapper);
    }

    // ── driver rides ─────────────────────────────────────────────────────────

    @Test
    void findDriverActiveTrips_shouldMapSeatAndEarningFields() {
        Trip trip = buildTrip("trip-1", TripStatus.AVAILABLE, 4, 2, 9.5);

        when(tripsRepository.findByDriverIdAndTripStatusIn(
                        "driver-1", List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS)))
                .thenReturn(List.of(trip));
        when(bookingRepository.findByTripIdInAndStatusIn(
                        List.of("trip-1"), List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of());
        when(passengerRepository.findByUserIdIn(List.of())).thenReturn(List.of());

        List<DriverTripResponse> responses = activeRidesService.findDriverActiveTrips("driver-1");

        assertEquals(1, responses.size());
        DriverTripResponse response = responses.getFirst();
        assertEquals("trip-1", response.tripId());
        assertEquals(2, response.availableSeats());
        assertEquals(2, response.bookedSeats());
        assertEquals(19.0, response.estimatedEarnings());
        assertTrue(response.passengers().isEmpty());

        verify(tripsRepository)
                .findByDriverIdAndTripStatusIn(
                        "driver-1", List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS));
        verify(bookingRepository)
                .findByTripIdInAndStatusIn(
                        List.of("trip-1"), List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS));
    }

    @Test
    void findDriverActiveTrips_shouldPopulatePassengerNamesAndStatus() {
        Trip trip = buildTrip("trip-1", TripStatus.AVAILABLE, 4, 1, 10.0);
        Booking booking = buildBooking("b-1", "trip-1", BookingStatus.CONFIRMED, "Europe/Berlin");
        Passenger passenger = buildPassenger("passenger-1", "Jane", "Doe");

        when(tripsRepository.findByDriverIdAndTripStatusIn(
                        "driver-1", List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS)))
                .thenReturn(List.of(trip));
        when(bookingRepository.findByTripIdInAndStatusIn(
                        List.of("trip-1"), List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of(booking));
        when(passengerRepository.findByUserIdIn(List.of("passenger-1"))).thenReturn(List.of(passenger));

        List<DriverTripResponse> responses = activeRidesService.findDriverActiveTrips("driver-1");

        assertEquals(1, responses.size());
        List<DriverTripResponse.PassengerInfo> passengers = responses.getFirst().passengers();
        assertEquals(1, passengers.size());
        DriverTripResponse.PassengerInfo info = passengers.getFirst();
        assertEquals("passenger-1", info.userId());
        assertEquals("Jane", info.firstName());
        assertEquals("Doe", info.lastName());
        assertEquals(BookingStatus.CONFIRMED, info.bookingStatus());
        assertEquals(2, info.bookedSeats());
    }

    @Test
    void findDriverActiveTrips_shouldReturnNullNames_whenPassengerNotInDb() {
        Trip trip = buildTrip("trip-1", TripStatus.AVAILABLE, 4, 1, 10.0);
        Booking booking = buildBooking("b-1", "trip-1", BookingStatus.CONFIRMED, "Europe/Berlin");

        when(tripsRepository.findByDriverIdAndTripStatusIn(
                        "driver-1", List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS)))
                .thenReturn(List.of(trip));
        when(bookingRepository.findByTripIdInAndStatusIn(
                        List.of("trip-1"), List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of(booking));
        when(passengerRepository.findByUserIdIn(List.of("passenger-1"))).thenReturn(List.of());

        List<DriverTripResponse> responses = activeRidesService.findDriverActiveTrips("driver-1");

        assertEquals(1, responses.size());
        DriverTripResponse.PassengerInfo info =
                responses.getFirst().passengers().getFirst();
        assertNull(info.firstName());
        assertNull(info.lastName());
        assertEquals(BookingStatus.CONFIRMED, info.bookingStatus());
        assertEquals("passenger-1", info.userId());
    }

    @Test
    void findDriverActiveTrips_shouldNotQueryPassengers_whenNoBookings() {
        Trip trip = buildTrip("trip-1", TripStatus.AVAILABLE, 4, 0, 10.0);

        when(tripsRepository.findByDriverIdAndTripStatusIn(
                        "driver-1", List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS)))
                .thenReturn(List.of(trip));
        when(bookingRepository.findByTripIdInAndStatusIn(
                        List.of("trip-1"), List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of());
        when(passengerRepository.findByUserIdIn(List.of())).thenReturn(List.of());

        List<DriverTripResponse> responses = activeRidesService.findDriverActiveTrips("driver-1");

        assertEquals(1, responses.size());
        assertTrue(responses.getFirst().passengers().isEmpty());
    }

    @Test
    void findPassengerActiveRides_whenTripExists_shouldPopulateCarType() {
        Booking booking = buildBooking("booking-1", "trip-1", BookingStatus.CONFIRMED, "Europe/Berlin");
        Trip trip = buildTrip("trip-1", TripStatus.AVAILABLE, 4, 1, 12.0);

        when(bookingRepository.findByPassengerIdAndStatusIn(
                        "passenger-1",
                        List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of(booking));
        when(tripsRepository.findAllById(List.of("trip-1"))).thenReturn(List.of(trip));

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

        when(bookingRepository.findByPassengerIdAndStatusIn(
                        "passenger-2",
                        List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)))
                .thenReturn(List.of(booking));
        when(tripsRepository.findAllById(List.of("missing-trip"))).thenReturn(List.of());

        List<PassengerRideResponse> responses = activeRidesService.findPassengerActiveRides("passenger-2");

        assertEquals(1, responses.size());
        assertEquals(null, responses.getFirst().carType());
    }

    // ── passenger rides ──────────────────────────────────────────────────────

    @Test
    void findPassengerRideHistory_shouldUseHistoryStatuses() {
        when(bookingRepository.findByPassengerIdAndStatusIn(
                        "passenger-3", List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED)))
                .thenReturn(List.of());

        List<PassengerRideResponse> responses = activeRidesService.findPassengerRideHistory("passenger-3");

        assertTrue(responses.isEmpty());
        verify(bookingRepository)
                .findByPassengerIdAndStatusIn("passenger-3", List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
    }

    @Test
    void findDriverTripHistory_shouldMapCompletedAndCancelledTrips() {
        Trip completed = buildTrip("trip-c", TripStatus.COMPLETED, 4, 4, 10.0);
        Trip cancelled = buildTrip("trip-x", TripStatus.CANCELLED, 4, 1, 10.0);

        when(tripsRepository.findByDriverIdAndTripStatusIn(
                        "driver-2", List.of(TripStatus.COMPLETED, TripStatus.CANCELLED)))
                .thenReturn(List.of(completed, cancelled));

        List<DriverTripResponse> responses = activeRidesService.findDriverTripHistory("driver-2");

        assertEquals(2, responses.size());
        assertEquals("trip-c", responses.getFirst().tripId());
        assertEquals(4, responses.getFirst().bookedSeats());
        assertEquals(40.0, responses.getFirst().estimatedEarnings());
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

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
        trip.setSourceAddress(Points.of(0, 0, ""));
        trip.setDestinationAddress(Points.of(0, 0, ""));
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
        booking.setRideStartTimeUTC(
                ZonedDateTime.of(2026, 3, 1, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant());
        booking.setPricePerSeat(12.5);
        booking.setPickupLocation(Points.of(0, 0, ""));
        booking.setDropoffLocation(Points.of(0, 0, ""));
        return booking;
    }

    private Passenger buildPassenger(String userId, String firstName, String lastName) {
        Passenger passenger = new Passenger();
        passenger.setUserId(userId);
        passenger.setFirstName(firstName);
        passenger.setLastName(lastName);
        return passenger;
    }
}
