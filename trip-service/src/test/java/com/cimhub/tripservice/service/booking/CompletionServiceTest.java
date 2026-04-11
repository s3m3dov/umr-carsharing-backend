package com.cimhub.tripservice.service.booking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.response.CompleteRideResponse;
import com.cimhub.tripservice.dto.response.CompleteTripResponse;
import com.cimhub.tripservice.exception.RideNotCompletableException;
import com.cimhub.tripservice.exception.TripNotInProgressException;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.notification.DriverNotificationService;
import com.cimhub.tripservice.service.trip.TripService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompletionServiceTest {

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

    private CompletionService completionService;

    @BeforeEach
    void setUp() {
        completionService = new CompletionService(
                bookingRepository, tripsRepository, tripService, auditService, driverNotificationService);
    }

    // ── completeRide ──────────────────────────────────────────────────────────

    @Test
    void completeRide_whenConfirmed_shouldCompleteAndNotify() {
        Booking booking = Booking.builder()
                .bookingId("b-1")
                .tripId("trip-1")
                .driverId("driver-1")
                .passengerId("passenger-1")
                .status(BookingStatus.CONFIRMED)
                .build();
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);

        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(booking));
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CompleteRideResponse result = completionService.completeRide("b-1", "driver-1");

        assertEquals("b-1", result.bookingId());
        assertEquals("trip-1", result.tripId());
        assertEquals("driver-1", result.driverId());
        assertEquals("passenger-1", result.passengerId());
        assertEquals(BookingStatus.COMPLETED, result.bookingStatus());
        assertEquals(TripStatus.IN_PROGRESS, result.tripStatus());
        assertNotNull(result.updatedAt());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(BookingStatus.COMPLETED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getUpdatedAt());

        verify(auditService).logComplete(EntityType.BOOKING, "b-1", "driver-1", "Ride completed by driver");
        verify(driverNotificationService).notifyRideCompleted(trip, booking);
    }

    @Test
    void completeRide_whenInProgress_shouldCompleteSuccessfully() {
        Booking booking = Booking.builder()
                .bookingId("b-2")
                .tripId("trip-1")
                .driverId("driver-1")
                .passengerId("p-2")
                .status(BookingStatus.IN_PROGRESS)
                .build();
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);

        when(bookingRepository.findById("b-2")).thenReturn(Optional.of(booking));
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CompleteRideResponse result = completionService.completeRide("b-2", "driver-1");

        assertEquals("b-2", result.bookingId());
        assertEquals(BookingStatus.COMPLETED, result.bookingStatus());
        assertEquals(TripStatus.IN_PROGRESS, result.tripStatus());
        assertNotNull(result.updatedAt());
    }

    @Test
    void completeRide_whenBookingNotFound_shouldThrowResourceNotFound() {
        when(bookingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> completionService.completeRide("missing", "driver-1"));
    }

    @Test
    void completeRide_whenDriverIsNotOwner_shouldThrowUnauthorized() {
        Booking booking = Booking.builder()
                .bookingId("b-1")
                .driverId("actual-driver")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () -> completionService.completeRide("b-1", "intruder"));
        verify(bookingRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = BookingStatus.class,
            names = {"CANCELLED", "REJECTED", "COMPLETED", "REQUESTED"})
    void completeRide_whenStatusNotCompletable_shouldThrowRideNotCompletable(BookingStatus status) {
        Booking booking = Booking.builder()
                .bookingId("b-1")
                .driverId("driver-1")
                .status(status)
                .build();

        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(booking));

        assertThrows(RideNotCompletableException.class, () -> completionService.completeRide("b-1", "driver-1"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void completeRide_whenTripNotFound_shouldThrowResourceNotFound() {
        Booking booking = Booking.builder()
                .bookingId("b-1")
                .tripId("ghost-trip")
                .driverId("driver-1")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(booking));
        when(tripsRepository.findById("ghost-trip")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> completionService.completeRide("b-1", "driver-1"));
    }

    @Test
    void completeRide_shouldNotSaveOrTouchTrip() {
        Booking booking = Booking.builder()
                .bookingId("b-1")
                .tripId("trip-1")
                .driverId("driver-1")
                .passengerId("p-1")
                .status(BookingStatus.CONFIRMED)
                .build();
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);

        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(booking));
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        completionService.completeRide("b-1", "driver-1");

        verify(tripsRepository, never()).save(any());
        verify(tripService, never()).updateStatus(any(), any());
    }

    @Test
    void completeRide_shouldSetUpdatedAt() {
        Booking booking = Booking.builder()
                .bookingId("b-1")
                .tripId("trip-1")
                .driverId("driver-1")
                .passengerId("p-1")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(booking));
        when(tripsRepository.findById("trip-1"))
                .thenReturn(Optional.of(tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS)));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        completionService.completeRide("b-1", "driver-1");

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertNotNull(captor.getValue().getUpdatedAt());
    }

    // ── completeTrip ──────────────────────────────────────────────────────────

    @Test
    void completeTrip_shouldCompleteRemainingRidesAndTrip() {
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);

        Booking confirmed = Booking.builder()
                .bookingId("b-1")
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking inProgress = Booking.builder()
                .bookingId("b-2")
                .status(BookingStatus.IN_PROGRESS)
                .build();
        Booking alreadyDone = Booking.builder()
                .bookingId("b-3")
                .status(BookingStatus.COMPLETED)
                .build();
        Booking cancelled = Booking.builder()
                .bookingId("b-4")
                .status(BookingStatus.CANCELLED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1"))
                .thenReturn(List.of(confirmed, inProgress, alreadyDone, cancelled));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trip completedTrip = tripWithStatus("trip-1", "driver-1", TripStatus.COMPLETED);
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip), Optional.of(completedTrip));

        CompleteTripResponse result = completionService.completeTrip("trip-1", "driver-1");

        assertEquals("trip-1", result.tripId());
        assertEquals("driver-1", result.driverId());
        assertEquals(TripStatus.COMPLETED, result.tripStatus());
        assertEquals(2, result.autoCompletedRides());
        assertNotNull(result.updatedAt());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(2)).save(captor.capture());
        captor.getAllValues().forEach(b -> assertEquals(BookingStatus.COMPLETED, b.getStatus()));

        verify(tripService).updateStatus("trip-1", TripStatus.COMPLETED);
        verify(auditService).logComplete(EntityType.TRIP, "trip-1", "driver-1", "Trip completed by driver");
        verify(auditService, times(2))
                .logComplete(
                        eq(EntityType.BOOKING), any(), eq("driver-1"), eq("Ride auto-completed on trip completion"));
        verify(driverNotificationService, times(2)).notifyTripCompleted(eq(trip), any(Booking.class));
    }

    @Test
    void completeTrip_withNoRemainingActiveRides_shouldCompleteTrip() {
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);

        Booking done = Booking.builder()
                .bookingId("b-1")
                .status(BookingStatus.COMPLETED)
                .build();

        Trip completedTrip = tripWithStatus("trip-1", "driver-1", TripStatus.COMPLETED);
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip), Optional.of(completedTrip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(done));

        CompleteTripResponse result = completionService.completeTrip("trip-1", "driver-1");

        assertEquals("trip-1", result.tripId());
        assertEquals(TripStatus.COMPLETED, result.tripStatus());
        assertEquals(0, result.autoCompletedRides());
        assertNotNull(result.updatedAt());
        verify(bookingRepository, never()).save(any());
        verify(tripService).updateStatus("trip-1", TripStatus.COMPLETED);
    }

    @Test
    void completeTrip_whenTripNotFound_shouldThrowResourceNotFound() {
        when(tripsRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> completionService.completeTrip("missing", "driver-1"));
    }

    @Test
    void completeTrip_whenDriverIsNotOwner_shouldThrowUnauthorized() {
        Trip trip = tripWithStatus("trip-1", "real-driver", TripStatus.IN_PROGRESS);
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        assertThrows(UnauthorizedException.class, () -> completionService.completeTrip("trip-1", "intruder"));
        verify(tripService, never()).updateStatus(any(), any());
    }

    @ParameterizedTest
    @EnumSource(
            value = TripStatus.class,
            names = {"CREATED", "COMPLETED", "CANCELLED"})
    void completeTrip_whenStatusNotCompletable_shouldThrowTripNotInProgress(TripStatus status) {
        Trip trip = tripWithStatus("trip-1", "driver-1", status);
        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        assertThrows(TripNotInProgressException.class, () -> completionService.completeTrip("trip-1", "driver-1"));
        verify(tripService, never()).updateStatus(any(), any());
    }

    @Test
    void completeTrip_whenAvailable_shouldTransitionAndComplete() {
        Trip availableTrip = tripWithStatus("trip-1", "driver-1", TripStatus.AVAILABLE);
        Trip completedTrip = tripWithStatus("trip-1", "driver-1", TripStatus.COMPLETED);
        Booking confirmed = Booking.builder()
                .bookingId("b-1")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(availableTrip), Optional.of(completedTrip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(confirmed));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CompleteTripResponse result = completionService.completeTrip("trip-1", "driver-1");

        assertEquals(TripStatus.COMPLETED, result.tripStatus());
        assertEquals(1, result.autoCompletedRides());
        verify(tripService).updateStatus("trip-1", TripStatus.IN_PROGRESS);
        verify(tripService).updateStatus("trip-1", TripStatus.COMPLETED);
    }

    @Test
    void completeTrip_withEmptyBookingList_shouldCompleteTrip() {
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);
        Trip completedTrip = tripWithStatus("trip-1", "driver-1", TripStatus.COMPLETED);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip), Optional.of(completedTrip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of());

        CompleteTripResponse result = completionService.completeTrip("trip-1", "driver-1");

        assertEquals("trip-1", result.tripId());
        assertEquals(TripStatus.COMPLETED, result.tripStatus());
        assertEquals(0, result.autoCompletedRides());
        assertNotNull(result.updatedAt());
        verify(bookingRepository, never()).save(any());
        verify(driverNotificationService, never()).notifyTripCompleted(any(), any());
        verify(tripService).updateStatus("trip-1", TripStatus.COMPLETED);
    }

    @Test
    void completeTrip_shouldSetUpdatedAtOnEachAutoCompletedBooking() {
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);
        Booking b1 = Booking.builder()
                .bookingId("b-1")
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking b2 = Booking.builder()
                .bookingId("b-2")
                .status(BookingStatus.IN_PROGRESS)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(b1, b2));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        completionService.completeTrip("trip-1", "driver-1");

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(2)).save(captor.capture());
        captor.getAllValues().forEach(b -> assertNotNull(b.getUpdatedAt()));
    }

    @Test
    void completeTrip_shouldNotifyEachAutoCompletedPassengerIndividually() {
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);
        Booking b1 = Booking.builder()
                .bookingId("b-1")
                .passengerId("p-1")
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking b2 = Booking.builder()
                .bookingId("b-2")
                .passengerId("p-2")
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking b3 = Booking.builder()
                .bookingId("b-3")
                .passengerId("p-3")
                .status(BookingStatus.CANCELLED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(b1, b2, b3));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        completionService.completeTrip("trip-1", "driver-1");

        verify(driverNotificationService, times(2)).notifyTripCompleted(eq(trip), any(Booking.class));
        verify(driverNotificationService, never()).notifyTripCompleted(eq(trip), eq(b3));
    }

    @Test
    void completeTrip_shouldAuditEachAutoCompletedBooking() {
        Trip trip = tripWithStatus("trip-1", "driver-1", TripStatus.IN_PROGRESS);
        Booking b1 = Booking.builder()
                .bookingId("b-1")
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking b2 = Booking.builder()
                .bookingId("b-2")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(bookingRepository.findByTripId("trip-1")).thenReturn(List.of(b1, b2));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        completionService.completeTrip("trip-1", "driver-1");

        verify(auditService)
                .logComplete(EntityType.BOOKING, "b-1", "driver-1", "Ride auto-completed on trip completion");
        verify(auditService)
                .logComplete(EntityType.BOOKING, "b-2", "driver-1", "Ride auto-completed on trip completion");
        verify(auditService).logComplete(EntityType.TRIP, "trip-1", "driver-1", "Trip completed by driver");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Trip tripWithStatus(String tripId, String driverId, TripStatus status) {
        Trip trip = new Trip();
        trip.setTripId(tripId);
        trip.setDriverId(driverId);
        trip.setTripStatus(status);
        return trip;
    }
}
