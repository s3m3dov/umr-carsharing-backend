package com.cimhub.tripservice.service.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.exception.AlreadyBookedException;
import com.cimhub.tripservice.exception.BookingAlreadyProcessedException;
import com.cimhub.tripservice.exception.InsufficientSeatsException;
import com.cimhub.tripservice.exception.TripNotAvailableException;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.notification.DriverNotificationService;
import com.cimhub.tripservice.testsupport.TripFixtures;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JoinTripServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private DriverNotificationService driverNotificationService;

    private JoinTripService joinTripService;

    @BeforeEach
    void setUp() {
        joinTripService =
                new JoinTripService(tripsRepository, bookingRepository, auditService, driverNotificationService);
    }

    // ── joinTrip (auto-confirm) ───────────────────────────────────────────────

    @Test
    void joinTrip_withAvailableSeats_shouldCreateConfirmedBookingAndIncrementBookedSeats() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 4, 1);
        JoinTripRequest request = TripFixtures.joinTripRequest("trip-1", "passenger-1", 2);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId("booking-new");
            return b;
        });

        String result = joinTripService.joinTrip("passenger-1", request);

        assertEquals("Ride booked successfully.", result);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking saved = bookingCaptor.getValue();
        assertEquals("trip-1", saved.getTripId());
        assertEquals("passenger-1", saved.getPassengerId());
        assertEquals("driver-1", saved.getDriverId());
        assertEquals(2, saved.getRequestedSeats());
        assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
        assertEquals(10.0, saved.getPricePerSeat());
        assertEquals(20.0, saved.getEstimatedPrice()); // 2 seats × 10.0

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        assertEquals(3, tripCaptor.getValue().getBookedSeats()); // 1 + 2

        verify(driverNotificationService).notifyBookingConfirmed(any(Trip.class), any(Booking.class));
    }

    @Test
    void joinTrip_withCreatedStatus_shouldAlsoBeAllowed() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.CREATED, 3, 0);
        JoinTripRequest request = TripFixtures.joinTripRequest("trip-1", "passenger-1", 1);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = joinTripService.joinTrip("passenger-1", request);

        assertEquals("Ride booked successfully.", result);
    }

    @Test
    void joinTrip_whenTripNotFound_shouldThrowResourceNotFoundException() {
        when(tripsRepository.findById("missing-trip")).thenReturn(Optional.empty());

        JoinTripRequest request = TripFixtures.joinTripRequest("missing-trip", "passenger-1", 1);

        assertThrows(ResourceNotFoundException.class, () -> joinTripService.joinTrip("passenger-1", request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTrip_whenTripNotAvailable_shouldThrowTripNotAvailableException() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.CANCELLED, 4, 0);
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        JoinTripRequest request = TripFixtures.joinTripRequest("trip-1", "passenger-1", 1);

        assertThrows(TripNotAvailableException.class, () -> joinTripService.joinTrip("passenger-1", request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTrip_whenPassengerAlreadyHasActiveBooking_shouldThrowAlreadyBookedException() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 4, 1);
        Booking existing = Booking.builder()
                .bookingId("b-existing")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(existing));

        JoinTripRequest request = TripFixtures.joinTripRequest("trip-1", "passenger-1", 1);

        assertThrows(AlreadyBookedException.class, () -> joinTripService.joinTrip("passenger-1", request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTrip_whenPassengerHasOnlyCancelledBooking_shouldAllowRebook() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 4, 0);
        Booking cancelled = Booking.builder()
                .bookingId("b-old")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .status(BookingStatus.CANCELLED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(cancelled));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        String result =
                joinTripService.joinTrip("passenger-1", TripFixtures.joinTripRequest("trip-1", "passenger-1", 1));

        assertEquals("Ride booked successfully.", result);
    }

    @Test
    void joinTrip_whenInsufficientSeats_shouldThrowInsufficientSeatsException() {
        // totalSeats=3, bookedSeats=2 → only 1 available, requesting 2
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 3, 2);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());

        JoinTripRequest request = TripFixtures.joinTripRequest("trip-1", "passenger-1", 2);

        assertThrows(InsufficientSeatsException.class, () -> joinTripService.joinTrip("passenger-1", request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTrip_whenExactlyEnoughSeats_shouldSucceed() {
        // totalSeats=3, bookedSeats=1 → exactly 2 available, requesting 2
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 3, 1);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        String result =
                joinTripService.joinTrip("passenger-1", TripFixtures.joinTripRequest("trip-1", "passenger-1", 2));

        assertEquals("Ride booked successfully.", result);
    }

    // ── joinTripWithDriverAccept ──────────────────────────────────────────────

    @Test
    void joinTripWithDriverAccept_withAvailableSeats_shouldCreateRequestedBookingAndIncrementBookedSeats() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 4, 1);
        JoinTripRequest request = TripFixtures.joinTripRequest("trip-1", "passenger-1", 2);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId("booking-new");
            return b;
        });

        String result = joinTripService.joinTripWithDriverAccept("passenger-1", request);

        assertEquals("Booking request submitted. Waiting for driver confirmation.", result);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking saved = bookingCaptor.getValue();
        assertEquals("trip-1", saved.getTripId());
        assertEquals("passenger-1", saved.getPassengerId());
        assertEquals("driver-1", saved.getDriverId());
        assertEquals(2, saved.getRequestedSeats());
        assertEquals(BookingStatus.REQUESTED, saved.getStatus());
        assertEquals(10.0, saved.getPricePerSeat());
        assertEquals(20.0, saved.getEstimatedPrice()); // 2 seats × 10.0

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        assertEquals(3, tripCaptor.getValue().getBookedSeats()); // 1 + 2

        verify(driverNotificationService).notifyBookingRequested(any(Trip.class), any(Booking.class));
    }

    @Test
    void joinTripWithDriverAccept_withCreatedStatus_shouldAlsoBeAllowed() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.CREATED, 3, 0);
        JoinTripRequest request = TripFixtures.joinTripRequest("trip-1", "passenger-1", 1);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = joinTripService.joinTripWithDriverAccept("passenger-1", request);

        assertEquals("Booking request submitted. Waiting for driver confirmation.", result);
    }

    @Test
    void joinTripWithDriverAccept_whenTripNotFound_shouldThrowResourceNotFoundException() {
        when(tripsRepository.findById("missing-trip")).thenReturn(Optional.empty());

        JoinTripRequest request = TripFixtures.joinTripRequest("missing-trip", "passenger-1", 1);

        assertThrows(
                ResourceNotFoundException.class,
                () -> joinTripService.joinTripWithDriverAccept("passenger-1", request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTripWithDriverAccept_whenTripNotAvailable_shouldThrowTripNotAvailableException() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.CANCELLED, 4, 0);
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        assertThrows(
                TripNotAvailableException.class,
                () -> joinTripService.joinTripWithDriverAccept(
                        "passenger-1", TripFixtures.joinTripRequest("trip-1", "passenger-1", 1)));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTripWithDriverAccept_whenPassengerAlreadyHasActiveBooking_shouldThrowAlreadyBookedException() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 4, 1);
        Booking existing = Booking.builder()
                .bookingId("b-existing")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .status(BookingStatus.REQUESTED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(existing));

        assertThrows(
                AlreadyBookedException.class,
                () -> joinTripService.joinTripWithDriverAccept(
                        "passenger-1", TripFixtures.joinTripRequest("trip-1", "passenger-1", 1)));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTripWithDriverAccept_whenInsufficientSeats_shouldThrowInsufficientSeatsException() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 3, 2);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());

        assertThrows(
                InsufficientSeatsException.class,
                () -> joinTripService.joinTripWithDriverAccept(
                        "passenger-1", TripFixtures.joinTripRequest("trip-1", "passenger-1", 2)));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void joinTripWithDriverAccept_whenPassengerHasOnlyCancelledBooking_shouldAllowRebook() {
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 4, 0);
        Booking cancelled = Booking.builder()
                .bookingId("b-old")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .status(BookingStatus.CANCELLED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(cancelled));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = joinTripService.joinTripWithDriverAccept(
                "passenger-1", TripFixtures.joinTripRequest("trip-1", "passenger-1", 1));

        assertEquals("Booking request submitted. Waiting for driver confirmation.", result);
    }

    @Test
    void joinTripWithDriverAccept_whenExactlyEnoughSeats_shouldSucceed() {
        // totalSeats=3, bookedSeats=1 → exactly 2 available, requesting 2
        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 3, 1);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = joinTripService.joinTripWithDriverAccept(
                "passenger-1", TripFixtures.joinTripRequest("trip-1", "passenger-1", 2));

        assertEquals("Booking request submitted. Waiting for driver confirmation.", result);
    }

    // ── acceptBooking ─────────────────────────────────────────────────────────

    @Test
    void acceptBooking_withRequestedBooking_shouldSetConfirmedAndAudit() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .driverId("driver-1")
                .status(BookingStatus.REQUESTED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = joinTripService.acceptBooking("booking-1", "driver-1");

        assertEquals("Booking accepted successfully", result);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(BookingStatus.CONFIRMED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getUpdatedAt());
    }

    @Test
    void acceptBooking_whenNotFound_shouldThrowResourceNotFoundException() {
        when(bookingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> joinTripService.acceptBooking("missing", "driver-1"));
    }

    @Test
    void acceptBooking_whenAlreadyConfirmed_shouldThrowBookingAlreadyProcessedException() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .driverId("driver-1")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        assertThrows(
                BookingAlreadyProcessedException.class, () -> joinTripService.acceptBooking("booking-1", "driver-1"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void acceptBooking_whenAlreadyCancelled_shouldThrowBookingAlreadyProcessedException() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .driverId("driver-1")
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        assertThrows(
                BookingAlreadyProcessedException.class, () -> joinTripService.acceptBooking("booking-1", "driver-1"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void acceptBooking_whenDriverIsNotOwner_shouldThrowUnauthorizedException() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .driverId("driver-1")
                .status(BookingStatus.REQUESTED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () -> joinTripService.acceptBooking("booking-1", "driver-2"));
        verify(bookingRepository, never()).save(any());
    }

    // ── rejectBooking ─────────────────────────────────────────────────────────

    @Test
    void rejectBooking_withRequestedBooking_shouldSetRejectedAndReleaseTripSeats() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .driverId("driver-1")
                .requestedSeats(2)
                .status(BookingStatus.REQUESTED)
                .build();

        Trip trip = TripFixtures.trip("trip-1", "driver-1", TripStatus.AVAILABLE, 4, 3);

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = joinTripService.rejectBooking("booking-1", "driver-1");

        assertEquals("Booking rejected", result);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertEquals(BookingStatus.REJECTED, bookingCaptor.getValue().getStatus());

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        assertEquals(1, tripCaptor.getValue().getBookedSeats()); // 3 - 2
    }

    @Test
    void rejectBooking_whenTripNotFound_shouldStillRejectBookingWithoutCrashing() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .tripId("ghost-trip")
                .driverId("driver-1")
                .requestedSeats(1)
                .status(BookingStatus.REQUESTED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tripsRepository.findById("ghost-trip")).thenReturn(Optional.empty());

        String result = joinTripService.rejectBooking("booking-1", "driver-1");

        assertEquals("Booking rejected", result);
        verify(tripsRepository, never()).save(any());
    }

    @Test
    void rejectBooking_whenNotFound_shouldThrowResourceNotFoundException() {
        when(bookingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> joinTripService.rejectBooking("missing", "driver-1"));
    }

    @Test
    void rejectBooking_whenNotInRequestedState_shouldThrowBookingAlreadyProcessedException() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .driverId("driver-1")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        assertThrows(
                BookingAlreadyProcessedException.class, () -> joinTripService.rejectBooking("booking-1", "driver-1"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectBooking_whenDriverIsNotOwner_shouldThrowUnauthorizedException() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .driverId("driver-1")
                .status(BookingStatus.REQUESTED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () -> joinTripService.rejectBooking("booking-1", "driver-2"));
        verify(bookingRepository, never()).save(any());
    }
}
