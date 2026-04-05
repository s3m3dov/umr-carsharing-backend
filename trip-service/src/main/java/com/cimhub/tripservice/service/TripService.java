package com.cimhub.tripservice.service;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.response.StartTripResponse;
import com.cimhub.tripservice.exception.InvalidTripStateException;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripsRepository tripsRepository;

    private static final Map<TripStatus, Set<TripStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(TripStatus.CREATED, EnumSet.of(TripStatus.AVAILABLE, TripStatus.CANCELLED));
        VALID_TRANSITIONS.put(TripStatus.AVAILABLE, EnumSet.of(TripStatus.IN_PROGRESS, TripStatus.CANCELLED));
        VALID_TRANSITIONS.put(TripStatus.IN_PROGRESS, EnumSet.of(TripStatus.COMPLETED, TripStatus.CANCELLED));
        VALID_TRANSITIONS.put(TripStatus.COMPLETED, EnumSet.noneOf(TripStatus.class));
        VALID_TRANSITIONS.put(TripStatus.CANCELLED, EnumSet.noneOf(TripStatus.class));
    }

    @Transactional
    public void updateStatus(String tripId, TripStatus newStatus) {
        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        validateTransition(trip.getTripStatus(), newStatus);

        log.info("Transitioning trip {} from {} to {}", tripId, trip.getTripStatus(), newStatus);
        trip.setTripStatus(newStatus);
        trip.setUpdatedAt(Instant.now());
        tripsRepository.save(trip);
    }

    @Transactional
    public StartTripResponse startTrip(String tripId, String driverId) {
        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (!trip.getDriverId().equals(driverId)) {
            throw new UnauthorizedException("You are not authorized to start this trip");
        }

        if (trip.getTripStatus() != TripStatus.AVAILABLE) {
            throw new InvalidTripStateException(
                    String.format("Cannot start trip - current status: %s", trip.getTripStatus()));
        }

        trip.setTripStatus(TripStatus.IN_PROGRESS);
        trip.setUpdatedAt(Instant.now());
        tripsRepository.save(trip);

        log.info("Driver {} started trip {}", driverId, tripId);
        return StartTripResponse.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .tripStatus(trip.getTripStatus())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    private void validateTransition(TripStatus currentStatus, TripStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        Set<TripStatus> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new InvalidTripStateException(
                    String.format("Cannot transition trip from %s to %s", currentStatus, newStatus));
        }
    }
}
