package com.cimhub.tripservice.service;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.response.StartTripResponse;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.exception.InvalidTripStateException;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.trip.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private TripMapper tripMapper;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripService(tripsRepository, tripMapper);
    }

    @Test
    void updateStatus_whenValidTransition_shouldPersistNewStatus() {
        Trip trip = new Trip();
        trip.setTripId("trip-1");
        trip.setTripStatus(TripStatus.CREATED);
        trip.setUpdatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tripService.updateStatus("trip-1", TripStatus.AVAILABLE);

        assertEquals(TripStatus.AVAILABLE, trip.getTripStatus());
        assertNotNull(trip.getUpdatedAt());
        verify(tripsRepository).save(trip);
    }

    @Test
    void updateStatus_whenSameStatus_shouldAllowAndPersist() {
        Trip trip = new Trip();
        trip.setTripId("trip-2");
        trip.setTripStatus(TripStatus.AVAILABLE);

        when(tripsRepository.findById("trip-2")).thenReturn(Optional.of(trip));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tripService.updateStatus("trip-2", TripStatus.AVAILABLE);

        assertEquals(TripStatus.AVAILABLE, trip.getTripStatus());
        verify(tripsRepository).save(trip);
    }

    @Test
    void updateStatus_whenInvalidTransition_shouldThrowInvalidTripState() {
        Trip trip = new Trip();
        trip.setTripId("trip-3");
        trip.setTripStatus(TripStatus.COMPLETED);

        when(tripsRepository.findById("trip-3")).thenReturn(Optional.of(trip));

        assertThrows(InvalidTripStateException.class,
                () -> tripService.updateStatus("trip-3", TripStatus.CANCELLED));

        verify(tripsRepository, never()).save(any());
    }

    @Test
    void updateStatus_whenTripMissing_shouldThrowResourceNotFound() {
        when(tripsRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.updateStatus("missing", TripStatus.AVAILABLE));
    }

    @Test
    void updateStatus_whenTransitionBackwards_shouldThrowInvalidTripState() {
        Trip trip = new Trip();
        trip.setTripId("trip-4");
        trip.setTripStatus(TripStatus.AVAILABLE);

        when(tripsRepository.findById("trip-4")).thenReturn(Optional.of(trip));

        InvalidTripStateException ex = assertThrows(InvalidTripStateException.class,
                () -> tripService.updateStatus("trip-4", TripStatus.CREATED));

        assertTrue(ex.getMessage().contains("Cannot transition trip"));
        verify(tripsRepository, never()).save(any());
    }

    @Test
    void startTrip_whenAvailable_shouldStartSuccessfully() {
        Trip trip = Trip.builder()
                .tripId("trip-1")
                .driverId("driver-1")
                .tripStatus(TripStatus.AVAILABLE)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StartTripResponse result = tripService.startTrip("trip-1", "driver-1");

        assertEquals("trip-1", result.tripId());
        assertEquals("driver-1", result.driverId());
        assertEquals(TripStatus.IN_PROGRESS, result.tripStatus());
        assertNotNull(result.updatedAt());
        assertEquals(TripStatus.IN_PROGRESS, trip.getTripStatus());
        verify(tripsRepository).save(trip);
    }

    @Test
    void startTrip_whenNotOwner_shouldThrowUnauthorized() {
        Trip trip = Trip.builder()
                .tripId("trip-1")
                .driverId("driver-1")
                .tripStatus(TripStatus.AVAILABLE)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        assertThrows(UnauthorizedException.class,
                () -> tripService.startTrip("trip-1", "other-driver"));

        verify(tripsRepository, never()).save(any());
    }

    @Test
    void startTrip_whenNotAvailable_shouldThrowInvalidTripState() {
        Trip trip = Trip.builder()
                .tripId("trip-1")
                .driverId("driver-1")
                .tripStatus(TripStatus.IN_PROGRESS)
                .build();

        when(tripsRepository.findById("trip-1")).thenReturn(Optional.of(trip));

        assertThrows(InvalidTripStateException.class,
                () -> tripService.startTrip("trip-1", "driver-1"));

        verify(tripsRepository, never()).save(any());
    }

    @Test
    void startTrip_whenTripMissing_shouldThrowResourceNotFound() {
        when(tripsRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripService.startTrip("missing", "driver-1"));
    }
}
