package com.cimhub.tripservice.service.trip;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.exception.AlreadyCancelledException;
import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTripService {

    private final TripsRepository tripsRepository;
    private final AuditService auditService;
    private final TripMapper tripMapper;

    public Page<AdminTripResponse> listTrips(Pageable pageable) {
        return tripsRepository.findAll(pageable).map(tripMapper::toAdminTripResponse);
    }

    public AdminTripResponse getTrip(String tripId) {
        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        return tripMapper.toAdminTripResponse(trip);
    }

    public Page<AdminTripResponse> getUpcomingTrips(Pageable pageable) {
        return tripsRepository.findByTripStatusInAndTripStartDateTimeUTCAfter(
                List.of(TripStatus.CREATED, TripStatus.AVAILABLE), Instant.now(), pageable
        ).map(tripMapper::toAdminTripResponse);
    }

    public Page<AdminTripResponse> getTripHistory(Pageable pageable) {
        return tripsRepository.findByTripStatusIn(
                List.of(TripStatus.COMPLETED, TripStatus.CANCELLED), pageable
        ).map(tripMapper::toAdminTripResponse);
    }

    public void cancelTrip(String tripId, String adminEmail) {
        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));

        if (trip.getTripStatus() == TripStatus.CANCELLED) {
            throw new AlreadyCancelledException("This trip has already been cancelled");
        }

        trip.setTripStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(Instant.now());
        tripsRepository.save(trip);
        auditService.logCancel(EntityType.TRIP, tripId, adminEmail, "Trip cancelled by admin");
        log.info("Trip {} cancelled by admin {}", tripId, adminEmail);
    }
}
