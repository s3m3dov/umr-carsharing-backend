package com.cimhub.tripservice.service.trip;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.exception.AlreadyCancelledException;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.service.util.TimezoneConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTripServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private AuditService auditService;

    private AdminTripService adminTripService;

    private TripMapper tripMapper;

    @BeforeEach
    void setUp() {
        tripMapper = new TripMapper(new TimezoneConversionService());
        adminTripService = new AdminTripService(tripsRepository, auditService, tripMapper);
    }

    @Test
    void cancelTrip_shouldMarkCancelledAndWriteAuditLog() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setTripStatus(TripStatus.AVAILABLE);
        trip.setUpdatedAt(Instant.parse("2030-01-01T00:00:00Z"));

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        adminTripService.cancelTrip("trip-1", "admin@example.com");

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();

        assertEquals(TripStatus.CANCELLED, savedTrip.getTripStatus());
        assertNotNull(savedTrip.getUpdatedAt());

        verify(auditService).logCancel(EntityType.TRIP, "trip-1", "admin@example.com", "Trip cancelled by admin");
    }

    @Test
    void getTrip_whenNotFound_shouldThrowResourceNotFound() {
        when(tripsRepository.findById("missing-trip")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminTripService.getTrip("missing-trip"));
    }

    @Test
    void getTrip_shouldReturnMappedResponse() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setDriverId("driver-1");
        trip.setTripStatus(TripStatus.AVAILABLE);
        trip.setTotalSeats(4);
        trip.setBookedSeats(1);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        AdminTripResponse response = adminTripService.getTrip("trip-1");

        assertEquals("trip-1", response.tripId());
        assertEquals("driver-1", response.driverId());
        assertEquals(TripStatus.AVAILABLE, response.tripStatus());
        assertEquals(4, response.totalSeats());
        assertEquals(1, response.bookedSeats());
        assertEquals(3, response.availableSeats()); // totalSeats - bookedSeats
    }

    @Test
    void cancelTrip_whenNotFound_shouldThrowResourceNotFound() {
        when(tripsRepository.findById("missing-trip")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminTripService.cancelTrip("missing-trip", "admin@example.com"));
    }

    @Test
    void cancelTrip_whenAlreadyCancelled_shouldThrowAlreadyCancelled() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setTripStatus(TripStatus.CANCELLED);

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        assertThrows(AlreadyCancelledException.class, () ->
                adminTripService.cancelTrip("trip-1", "admin@example.com"));

        verify(tripsRepository, never()).save(any(Trip.class));
        verify(auditService, never()).logCancel(any(), any(), any(), any());
    }

    @Test
    void cancelTrip_shouldSetStatusAndUpdatedAtAndAudit() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setTripStatus(TripStatus.AVAILABLE);
        trip.setUpdatedAt(Instant.parse("2030-01-01T00:00:00Z"));

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        adminTripService.cancelTrip("trip-1", "admin@example.com");

        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(captor.capture());
        assertEquals(TripStatus.CANCELLED, captor.getValue().getTripStatus());
        assertNotNull(captor.getValue().getUpdatedAt());

        verify(auditService).logCancel(EntityType.TRIP, "trip-1", "admin@example.com", "Trip cancelled by admin");
    }

    @Test
    void listTrips_getUpcomingTrips_andGetTripHistory_shouldMapPages() {
        Pageable pageable = PageRequest.of(0, 10);
        Trip trip = new Trip();
        trip.setTripId("trip-10");
        trip.setDriverId("driver-10");
        trip.setVehicleNumber("HE-XX-1000");
        trip.setCarType("SUV");
        trip.setTripStatus(TripStatus.AVAILABLE);
        trip.setTotalSeats(5);
        trip.setBookedSeats(2);
        trip.setPricePerSeat(12.0);
        trip.setRouteDistance(12000.0);
        trip.setRouteDuration(1800.0);
        trip.setTripStartDateTimeUTC(Instant.parse("2026-01-01T12:00:00Z"));
        trip.setTripTimezone("UTC");
        trip.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        trip.setUpdatedAt(Instant.parse("2026-01-01T11:00:00Z"));

        Page<Trip> tripPage = new PageImpl<>(List.of(trip), pageable, 1);
        when(tripsRepository.findAll(pageable)).thenReturn(tripPage);
        when(tripsRepository.findByTripStatusInAndTripStartDateTimeUTCAfter(any(), any(), eq(pageable))).thenReturn(tripPage);
        when(tripsRepository.findByTripStatusIn(any(), eq(pageable))).thenReturn(tripPage);

        Page<AdminTripResponse> listed = adminTripService.listTrips(pageable);
        Page<AdminTripResponse> upcoming = adminTripService.getUpcomingTrips(pageable);
        Page<AdminTripResponse> history = adminTripService.getTripHistory(pageable);

        assertEquals(1, listed.getTotalElements());
        assertEquals("trip-10", listed.getContent().getFirst().tripId());
        assertEquals(3, listed.getContent().getFirst().availableSeats());
        assertEquals(12000.0, listed.getContent().getFirst().routeDistance());

        assertEquals(1, upcoming.getTotalElements());
        assertEquals("trip-10", upcoming.getContent().getFirst().tripId());

        assertEquals(1, history.getTotalElements());
        assertEquals("trip-10", history.getContent().getFirst().tripId());
    }
}
