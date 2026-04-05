package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
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

    public Page<AdminTripResponse> listTrips(Pageable pageable) {
        return tripsRepository.findAll(pageable).map(this::mapToResponse);
    }

    public AdminTripResponse getTrip(String tripId) {
        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        return mapToResponse(trip);
    }

    public Page<AdminTripResponse> getUpcomingTrips(Pageable pageable) {
        return tripsRepository.findByTripStatusInAndTripStartDateTimeUTCAfter(
                List.of(TripStatus.CREATED, TripStatus.AVAILABLE), Instant.now(), pageable
        ).map(this::mapToResponse);
    }

    public Page<AdminTripResponse> getTripHistory(Pageable pageable) {
        return tripsRepository.findByTripStatusIn(
                List.of(TripStatus.COMPLETED, TripStatus.CANCELLED), pageable
        ).map(this::mapToResponse);
    }

    public void cancelTrip(String tripId, String adminEmail) {
        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        trip.setTripStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(Instant.now());
        tripsRepository.save(trip);
        auditService.logCancel(EntityType.TRIP, tripId, adminEmail, "Trip cancelled by admin");
        log.info("Trip {} cancelled by admin {}", tripId, adminEmail);
    }

    private AdminTripResponse mapToResponse(Trip trip) {
        return AdminTripResponse.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .carType(trip.getCarType())
                .tripStatus(trip.getTripStatus())
                .sourceAddress(trip.getSourceAddress())
                .destinationAddress(trip.getDestinationAddress())
                .totalSeats(trip.getTotalSeats())
                .bookedSeats(trip.getBookedSeats())
                .availableSeats(trip.getTotalSeats() - trip.getBookedSeats())
                .pricePerSeat(trip.getPricePerSeat())
                .routeDistance(trip.getRouteDistance())
                .routeDuration(trip.getRouteDuration())
                .tripStartDateTimeUTC(trip.getTripStartDateTimeUTC())
                .tripTimezone(trip.getTripTimezone())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }
}
