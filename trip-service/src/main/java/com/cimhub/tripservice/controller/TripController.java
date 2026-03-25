package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.OfferRideService;
import com.cimhub.tripservice.service.TripGeospatialService;
import com.cimhub.tripservice.service.ActiveRidesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Trip Management", description = "APIs for drivers to create trips (offers), search available trips, cancel trips, and view trip history. Includes geospatial search capabilities.")
public class TripController {

    private final OfferRideService offerRideService;
    private final CancellationService cancellationService;
    private final TripGeospatialService geoService;
    private final ActiveRidesService activeRidesService;

    @Operation(
            summary = "Offer a new trip (driver creates a ride)",
            description = "Driver creates a trip with locations, departure time, and available seats. Trip becomes searchable by passengers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Trip created successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiExamples.OFFER_TRIP_RESPONSE))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid trip data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/offer")
    public ResponseEntity<ApiResponseWrapper<OfferRideResponse>> offerTrip(
            @Valid @RequestBody OfferRideRequest request) {
        log.info("Offering trip for driver: {}", request.driverId());
        OfferRideResponse response = offerRideService.offerRide(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Cancel a trip or booking",
            description = "Driver cancels entire trip or passenger cancels their booking."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cancelled successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiExamples.CANCEL_TRIP_RESPONSE))
            ),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "409", description = "Cannot cancel (already in progress)")
    })
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponseWrapper<String>> cancelTrip(
            @Valid @RequestBody CancelTripRequest request) {
        log.info("Cancelling trip: {} for user: {}", request.tripId(), request.userId());
        String response = cancellationService.cancelTrip(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Search trips near pickup location",
            description = "Finds trips with pickup within radius. Default 5km."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trips found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiExamples.SEARCH_TRIPS_RESPONSE))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or radius")
    })
    @GetMapping("/search/near-source")
    public ResponseEntity<ApiResponseWrapper<List<Trip>>> searchNearSource(
            @Parameter(description = "Latitude of the pickup location", example = "50.8154") @RequestParam double latitude,
            @Parameter(description = "Longitude of the pickup location", example = "8.6747") @RequestParam double longitude,
            @Parameter(description = "Search radius in kilometers (default 5.0)", example = "5.0") @RequestParam(defaultValue = "5.0") double radiusKm) {
        log.info("Searching for trips near source: ({}, {})", latitude, longitude);
        List<Trip> trips = geoService.findTripsWithSourceNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponseWrapper.success(trips));
    }

    @Operation(
            summary = "Search trips near dropoff location",
            description = "Finds trips with dropoff within radius. Default 5km."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trips found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiExamples.SEARCH_TRIPS_RESPONSE))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or radius")
    })
    @GetMapping("/search/near-destination")
    public ResponseEntity<ApiResponseWrapper<List<Trip>>> searchNearDestination(
            @Parameter(description = "Latitude of the dropoff location", example = "50.7265") @RequestParam double latitude,
            @Parameter(description = "Longitude of the dropoff location", example = "7.0895") @RequestParam double longitude,
            @Parameter(description = "Search radius in kilometers (default 5.0)", example = "5.0") @RequestParam(defaultValue = "5.0") double radiusKm) {
        log.info("Searching for trips near destination: ({}, {})", latitude, longitude);
        List<Trip> trips = geoService.findTripsWithDestinationNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponseWrapper.success(trips));
    }

    @Operation(
            summary = "Search trips matching a route",
            description = "Advanced search: finds trips matching pickup and dropoff with optional time filter."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Matching trips found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiExamples.SEARCH_TRIPS_RESPONSE))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    @GetMapping("/search/matching-route")
    public ResponseEntity<ApiResponseWrapper<List<Trip>>> searchMatchingRoute(
            @Parameter(description = "Latitude of pickup location", example = "50.8154") @RequestParam double sourceLatitude,
            @Parameter(description = "Longitude of pickup location", example = "8.6747") @RequestParam double sourceLongitude,
            @Parameter(description = "Radius around pickup in km", example = "5.0") @RequestParam(defaultValue = "5.0") double sourceRadiusKm,
            @Parameter(description = "Latitude of dropoff location", example = "50.7265") @RequestParam double destinationLatitude,
            @Parameter(description = "Longitude of dropoff location", example = "7.0895") @RequestParam double destinationLongitude,
            @Parameter(description = "Radius around dropoff in km", example = "5.0") @RequestParam(defaultValue = "5.0") double destinationRadiusKm,
            @Parameter(description = "Earliest departure time (ISO 8601 format, optional)", example = "2024-03-25T08:00:00Z") @RequestParam(required = false) String earliestDepartureTime,
            @Parameter(description = "Latest departure time (ISO 8601 format, optional)", example = "2024-03-25T18:00:00Z") @RequestParam(required = false) String latestDepartureTime,
            @Parameter(description = "Number of seats required", example = "2") @RequestParam Integer requestedSeats,
            @Parameter(description = "User ID of the passenger searching", example = "user_456") @RequestParam String effectiveUserId) {
        log.info("Searching for matching route trips");

        if (earliestDepartureTime != null && latestDepartureTime != null) {
            log.info("Using time range: {} to {}", earliestDepartureTime, latestDepartureTime);
        } else {
            log.info("No time range specified, searching all available trips");
        }

        List<Trip> trips = geoService.findTripsMatchingRoute(
                sourceLatitude,
                sourceLongitude,
                sourceRadiusKm,
                destinationLatitude,
                destinationLongitude,
                destinationRadiusKm,
                earliestDepartureTime,
                latestDepartureTime,
                requestedSeats,
                effectiveUserId);

        return ResponseEntity.ok(ApiResponseWrapper.success(trips));
    }

    @Operation(
            summary = "Get driver's active trips",
            description = "Retrieves active (scheduled or in-progress) trips for a driver."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active trips retrieved",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiExamples.ACTIVE_TRIPS_RESPONSE))
            ),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/active/driver/{driverId}")
    public ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> getDriverActiveTrips(
            @Parameter(description = "Driver's user ID", example = "user_123") @PathVariable String driverId) {
        log.info("Fetching active trips for driver: {}", driverId);
        List<DriverTripResponse> activeTrips = activeRidesService.findDriverActiveTrips(driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(activeTrips));
    }

    @Operation(
            summary = "Get driver's trip history",
            description = "Retrieves completed and cancelled trips for a driver."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trip history retrieved",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiExamples.ACTIVE_TRIPS_RESPONSE))
            ),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/history/driver/{driverId}")
    public ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> getDriverTripHistory(
            @Parameter(description = "Driver's user ID", example = "user_123") @PathVariable String driverId) {
        log.info("Fetching trip history for driver: {}", driverId);
        List<DriverTripResponse> tripHistory = activeRidesService.findDriverTripHistory(driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(tripHistory));
    }
}
