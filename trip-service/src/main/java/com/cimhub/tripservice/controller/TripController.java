package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponse;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.OfferRideService;
import com.cimhub.tripservice.service.TripGeospatialService;
import com.cimhub.tripservice.service.UpcomingRidesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final OfferRideService offerRideService;
    private final CancellationService cancellationService;
    private final TripGeospatialService geoService;
    private final UpcomingRidesService upcomingRidesService;

    @PostMapping("/offer")
    public ResponseEntity<ApiResponse<OfferRideResponse>> offerTrip(
            @Valid @RequestBody OfferRideRequest request) {
        log.info("Offering trip for driver: {}", request.driverId());
        OfferRideResponse response = offerRideService.offerRide(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> cancelTrip(
            @Valid @RequestBody CancelTripRequest request) {
        log.info("Cancelling trip: {} for user: {}", request.tripId(), request.userId());
        String response = cancellationService.cancelTrip(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search/near-source")
    public ResponseEntity<ApiResponse<List<Trip>>> searchNearSource(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        log.info("Searching for trips near source: ({}, {})", latitude, longitude);
        List<Trip> trips = geoService.findTripsWithSourceNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @GetMapping("/search/near-destination")
    public ResponseEntity<ApiResponse<List<Trip>>> searchNearDestination(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        log.info("Searching for trips near destination: ({}, {})", latitude, longitude);
        List<Trip> trips = geoService.findTripsWithDestinationNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @GetMapping("/search/matching-route")
    public ResponseEntity<ApiResponse<List<Trip>>> searchMatchingRoute(
            @RequestParam double sourceLat,
            @RequestParam double sourceLon,
            @RequestParam(defaultValue = "5.0") double sourceRadiusKm,
            @RequestParam double destLat,
            @RequestParam double destLon,
            @RequestParam(defaultValue = "5.0") double destRadiusKm,
            @RequestParam String rideStartTime,
            @RequestParam Integer requestedSeats,
            @RequestParam String effectiveUserId) {
        log.info("Searching for matching route trips");
        List<Trip> trips = geoService.findTripsMatchingRoute(
                sourceLat, sourceLon, sourceRadiusKm,
                destLat, destLon, destRadiusKm, rideStartTime, requestedSeats, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @GetMapping("/upcoming/driver/{driverId}")
    public ResponseEntity<ApiResponse<List<DriverTripResponse>>> getDriverUpcomingTrips(
            @PathVariable String driverId) {
        log.info("Fetching upcoming trips for driver: {}", driverId);
        List<DriverTripResponse> upcomingTrips = upcomingRidesService.findDriverUpcomingTrips(driverId);
        return ResponseEntity.ok(ApiResponse.success(upcomingTrips));
    }
}
