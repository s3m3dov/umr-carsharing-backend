package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.RequestDTO;
import com.cimhub.common.dto.ResponseDTO;
import com.cimhub.tripservice.dto.CancelRequestDTO;
import com.cimhub.tripservice.dto.DriverUpcomingTripDTO;
import com.cimhub.tripservice.dto.OfferRideRequestDTO;
import com.cimhub.tripservice.dto.OfferRideResponseDTO;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.OfferRideService;
import com.cimhub.tripservice.service.TripGeospatialService;
import com.cimhub.tripservice.service.UpcomingRidesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consolidated controller for Trip management (driver-focused operations)
 * Handles: offering trips, cancelling trips, searching trips, viewing upcoming
 * trips
 */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final OfferRideService offerRideService;
    private final CancellationService cancellationService;
    private final TripGeospatialService geoService;
    private final UpcomingRidesService upcomingRidesService;

    // ==================== Trip Creation ====================

    /**
     * Driver offers a new trip
     * POST /api/trips/offer
     */
    @PostMapping("/offer")
    public ResponseEntity<ResponseDTO<OfferRideResponseDTO>> offerTrip(
            @RequestBody RequestDTO<OfferRideRequestDTO> request) {

        ResponseDTO<OfferRideResponseDTO> response = offerRideService.offerRide(request);
        OfferRideResponseDTO responseContent = response.getResponseContent();

        if (responseContent != null && responseContent.getTripCreated() != null && responseContent.getTripCreated()) {
            response.setSuccess(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.setSuccess(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== Trip Cancellation ====================

    /**
     * Driver cancels their trip (cancels all associated bookings)
     * POST /api/trips/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<ResponseDTO<String>> cancelTrip(
            @RequestBody RequestDTO<CancelRequestDTO> request) {

        ResponseDTO<String> response = cancellationService.cancelTrip(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== Trip Search ====================

    /**
     * Find trips starting near a given location
     * GET
     * /api/trips/search/near-source?latitude=48.1351&longitude=11.5820&radiusKm=10
     */
    @GetMapping("/search/near-source")
    public ResponseEntity<List<Trip>> searchNearSource(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {

        log.info("Searching for trips near source: ({}, {}) within {} km", latitude, longitude, radiusKm);
        List<Trip> trips = geoService.findTripsWithSourceNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(trips);
    }

    /**
     * Find trips ending near a given location
     * GET
     * /api/trips/search/near-destination?latitude=48.1351&longitude=11.5820&radiusKm=10
     */
    @GetMapping("/search/near-destination")
    public ResponseEntity<List<Trip>> searchNearDestination(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {

        log.info("Searching for trips near destination: ({}, {}) within {} km", latitude, longitude, radiusKm);
        List<Trip> trips = geoService.findTripsWithDestinationNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(trips);
    }

    /**
     * Find trips matching both source and destination within given radius
     * GET /api/trips/search/matching-route?sourceLat=48.3638&sourceLon=10.6866&...
     */
    @GetMapping("/search/matching-route")
    public ResponseEntity<List<Trip>> searchMatchingRoute(
            @RequestParam double sourceLat,
            @RequestParam double sourceLon,
            @RequestParam(defaultValue = "5.0") double sourceRadiusKm,
            @RequestParam double destLat,
            @RequestParam double destLon,
            @RequestParam(defaultValue = "5.0") double destRadiusKm,
            @RequestParam String rideStartTime,
            @RequestParam Integer requestedSeats,
            @RequestParam String effectiveUserId) {

        log.info("Searching for trips from ({}, {}) [{}km] to ({}, {}) [{}km]",
                sourceLat, sourceLon, sourceRadiusKm, destLat, destLon, destRadiusKm);

        List<Trip> trips = geoService.findTripsMatchingRoute(
                sourceLat, sourceLon, sourceRadiusKm,
                destLat, destLon, destRadiusKm, rideStartTime, requestedSeats, effectiveUserId);
        return ResponseEntity.ok(trips);
    }

    /**
     * Find trips starting within a bounding box area
     * GET /api/trips/search/in-area?minLat=47.0&minLon=10.0&maxLat=50.0&maxLon=13.0
     */
    @GetMapping("/search/in-area")
    public ResponseEntity<List<Trip>> searchInArea(
            @RequestParam double minLat,
            @RequestParam double minLon,
            @RequestParam double maxLat,
            @RequestParam double maxLon) {

        log.info("Searching for trips in area: ({}, {}) to ({}, {})", minLat, minLon, maxLat, maxLon);
        List<Trip> trips = geoService.findTripsInArea(minLat, minLon, maxLat, maxLon);
        return ResponseEntity.ok(trips);
    }

    // ==================== Driver's Upcoming Trips ====================

    /**
     * Get driver's upcoming trips
     * GET /api/trips/upcoming/driver/{driverId}
     */
    @GetMapping("/upcoming/driver/{driverId}")
    public ResponseEntity<List<DriverUpcomingTripDTO>> getDriverUpcomingTrips(
            @PathVariable String driverId) {

        List<DriverUpcomingTripDTO> upcomingTrips = upcomingRidesService.getDriverUpcomingTrips(driverId);
        return ResponseEntity.ok(upcomingTrips);
    }
}
