package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTripServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private AuditService auditService;

    private AdminTripService adminTripService;

    @BeforeEach
    void setUp() {
        adminTripService = new AdminTripService(tripsRepository, auditService);
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
}
