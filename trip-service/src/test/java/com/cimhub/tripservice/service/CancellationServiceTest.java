package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.exception.AlreadyCancelledException;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.notification.DriverNotificationService;
import com.cimhub.tripservice.service.trip.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancellationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private TripService tripService;

    @Mock
    private AuditService auditService;

    @Mock
    private DriverNotificationService driverNotificationService;

    private CancellationService cancellationService;

    @BeforeEach
    void setUp() {
        cancellationService = new CancellationService(
                bookingRepository,
                tripsRepository,
                tripService,
                auditService,
                driverNotificationService);
    }

    // ── cancelRide ───────────────────────────────────────────────────────────

    @Test
    void cancelRide_whenPassengerNotOwner_shouldThrowUnauthorized() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .passengerId("owner")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("intruder")
                .rideId("booking-1")
                .build();

        assertThrows(UnauthorizedException.class, () -> cancellationService.cancelRide(request));
    }

    @Test
    void cancelRide_whenAlreadyCancelled_shouldThrowDomainException() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .passengerId("passenger-1")
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("passenger-1")
                .rideId("booking-1")
                .build();

        assertThrows(AlreadyCancelledException.class, () -> cancellationService.cancelRide(request));
    }

    @Test
    void cancelRide_shouldCancelBookingUpdateTripAndNotifyDriver() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .driverId("driver-1")
                .requestedSeats(2)
                .status(BookingStatus.CONFIRMED)
                .build();

        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setDriverId("driver-1");
        trip.setBookedSeats(5);
        trip.setTripStatus(TripStatus.AVAILABLE);

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("passenger-1")
                .rideId("booking-1")
                .build();

        String result = cancellationService.cancelRide(request);

        assertEquals("Booking cancelled successfully", result);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertEquals(BookingStatus.CANCELLED, bookingCaptor.getValue().getStatus());
        assertNotNull(bookingCaptor.getValue().getUpdatedAt());

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        assertEquals(3, tripCaptor.getValue().getBookedSeats());
        assertNotNull(tripCaptor.getValue().getUpdatedAt());

        verify(auditService).logCancel(EntityType.BOOKING, "booking-1", "passenger-1", "Booking cancelled by passenger");
        verify(driverNotificationService).notifyBookingCancelledByPassenger(any(Trip.class), any(Booking.class));
    }

    @Test
    void cancelTrip_shouldCancelTripAndActiveBookings() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setDriverId("driver-1");
        trip.setTripStatus(TripStatus.AVAILABLE);

        Booking activeBooking = Booking.builder()
                .bookingId("b-1")
                .tripId("trip-1")
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking alreadyCancelled = Booking.builder()
                .bookingId("b-2")
                .tripId("trip-1")
                .status(BookingStatus.CANCELLED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(activeBooking, alreadyCancelled));

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("driver-1")
                .tripId("trip-1")
                .build();

        String result = cancellationService.cancelTrip(request);

        assertEquals("Trip cancelled successfully. 2 passenger bookings have been cancelled", result);
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertEquals(BookingStatus.CANCELLED, bookingCaptor.getValue().getStatus());

        verify(tripService).updateStatus("trip-1", TripStatus.CANCELLED);
        verify(auditService).logCancel(EntityType.TRIP, "trip-1", "driver-1", "Trip cancelled by driver");
    }

    @Test
    void cancelRide_whenBookingNotFound_shouldThrowResourceNotFound() {
        when(bookingRepository.findById("missing-booking")).thenReturn(Optional.empty());

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("passenger-1")
                .rideId("missing-booking")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> cancellationService.cancelRide(request));
    }

    @Test
    void cancelRide_withRequestedBooking_shouldCancelSuccessfully() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .tripId("trip-1")
                .passengerId("passenger-1")
                .driverId("driver-1")
                .requestedSeats(1)
                .status(BookingStatus.REQUESTED)
                .build();

        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setBookedSeats(2);

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = cancellationService.cancelRide(
                CancelTripRequest.builder().userId("passenger-1").rideId("booking-1").build());

        assertEquals("Booking cancelled successfully", result);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(BookingStatus.CANCELLED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getUpdatedAt());
    }

    // ── cancelTrip ───────────────────────────────────────────────────────────

    @Test
    void cancelRide_whenTripNotFound_shouldThrowResourceNotFound() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .tripId("ghost-trip")
                .passengerId("passenger-1")
                .requestedSeats(1)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tripsRepository.findById("ghost-trip")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cancellationService.cancelRide(
                CancelTripRequest.builder().userId("passenger-1").rideId("booking-1").build()));
    }

    @Test
    void cancelTrip_withNoBookings_shouldSucceedAndReturnZeroCount() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setDriverId("driver-1");
        trip.setTripStatus(TripStatus.AVAILABLE);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());

        String result = cancellationService.cancelTrip(
                CancelTripRequest.builder().userId("driver-1").tripId("trip-1").build());

        assertEquals("Trip cancelled successfully. 0 passenger bookings have been cancelled", result);
        verify(bookingRepository, never()).save(any());
        verify(tripService).updateStatus("trip-1", TripStatus.CANCELLED);
    }

    @Test
    void cancelTrip_shouldAuditEachCancelledBooking() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setDriverId("driver-1");
        trip.setTripStatus(TripStatus.AVAILABLE);

        Booking b1 = Booking.builder().bookingId("b-1").tripId("trip-1").status(BookingStatus.CONFIRMED).build();
        Booking b2 = Booking.builder().bookingId("b-2").tripId("trip-1").status(BookingStatus.REQUESTED).build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(b1, b2));

        cancellationService.cancelTrip(
                CancelTripRequest.builder().userId("driver-1").tripId("trip-1").build());

        verify(auditService).logCancel(EntityType.BOOKING, "b-1", "driver-1",
                "Booking cancelled due to trip cancellation by driver");
        verify(auditService).logCancel(EntityType.BOOKING, "b-2", "driver-1",
                "Booking cancelled due to trip cancellation by driver");
        verify(auditService).logCancel(EntityType.TRIP, "trip-1", "driver-1", "Trip cancelled by driver");
    }

    @Test
    void cancelTrip_whenTripNotFound_shouldThrowResourceNotFound() {
        when(tripsRepository.findById("missing-trip")).thenReturn(Optional.empty());

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("driver-1")
                .tripId("missing-trip")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> cancellationService.cancelTrip(request));
    }

    @Test
    void cancelTrip_whenUserIsNotDriver_shouldThrowUnauthorized() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setDriverId("driver-1");
        trip.setTripStatus(TripStatus.AVAILABLE);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("intruder")
                .tripId("trip-1")
                .build();

        assertThrows(UnauthorizedException.class, () -> cancellationService.cancelTrip(request));
        verify(tripService, never()).updateStatus(any(), any());
    }

    @Test
    void cancelTrip_whenTripAlreadyCancelled_shouldThrowAlreadyCancelled() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setDriverId("driver-1");
        trip.setTripStatus(TripStatus.CANCELLED);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        CancelTripRequest request = CancelTripRequest.builder()
                .userId("driver-1")
                .tripId("trip-1")
                .build();

        assertThrows(AlreadyCancelledException.class, () -> cancellationService.cancelTrip(request));
        verify(tripService, never()).updateStatus(any(), any());
    }
}
