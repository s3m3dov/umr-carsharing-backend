package com.cimhub.tripservice.controller;

import com.cimhub.tripservice.dto.examples.TripApiDoc;
import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.request.TripSearchCriteria;
import com.cimhub.tripservice.dto.response.CompleteTripResponse;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.dto.response.StartTripResponse;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.model.OperationMessage;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.booking.CompletionService;
import com.cimhub.tripservice.service.trip.OfferRideService;
import com.cimhub.tripservice.service.geo.TripGeospatialService;
import com.cimhub.tripservice.service.trip.TripService;
import com.cimhub.tripservice.service.trip.ActiveRidesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trip Management", description = "APIs for drivers to create trips (offers), manage their trips, cancel trips, and view trip history.")
public class TripController {

    private final OfferRideService offerRideService;
    private final CancellationService cancellationService;
    private final CompletionService completionService;
    private final TripGeospatialService geoService;
    private final ActiveRidesService activeRidesService;
    private final TripService tripService;

    @Operation(
            summary = "Offer a new trip (driver creates a ride)",
            description = "Driver creates a trip with locations, departure time, and available seats. Trip becomes searchable by passengers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Trip created successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.OFFER_TRIP_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid trip data",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_INVALID_TRIP_DATA))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_UNAUTHORIZED))
            )
    })
    @PostMapping("/offer")
    public ResponseEntity<ApiResponseWrapper<OfferRideResponse>> offerTrip(
            @RequestHeader("X-User-Id") String authenticatedUserId,
            @RequestHeader("X-User-Role") String authenticatedUserRole,
            @Valid @RequestBody OfferRideRequest request) {
        requireDriverRole(authenticatedUserRole);
        if (request.driverId() != null && !request.driverId().equals(authenticatedUserId)) {
            throw new UnauthorizedException("Driver identity mismatch");
        }

        OfferRideRequest effectiveRequest = OfferRideRequest.builder()
                .driverId(authenticatedUserId)
                .sourceAddress(request.sourceAddress())
                .destinationAddress(request.destinationAddress())
                .tripStartDateTime(request.tripStartDateTime())
                .totalSeats(request.totalSeats())
                .pricePerSeat(request.pricePerSeat())
                .build();

        log.info("Offering trip for driver: {}", authenticatedUserId);
        OfferRideResponse response = offerRideService.offerRide(effectiveRequest);
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
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.CANCEL_TRIP_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_TRIP_NOT_FOUND))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot cancel (already in progress)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_TRIP_IN_PROGRESS))
            )
    })
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponseWrapper<String>> cancelTrip(
            @RequestHeader("X-User-Id") String authenticatedUserId,
            @RequestHeader("X-User-Role") String authenticatedUserRole,
            @Valid @RequestBody CancelTripRequest request) {
        AuthorizationHelper.requireDriverOrPassengerRole(authenticatedUserRole);
        if (request.userId() != null && !request.userId().equals(authenticatedUserId)) {
            throw new UnauthorizedException("User identity mismatch");
        }

        CancelTripRequest effectiveRequest = CancelTripRequest.builder()
                .userId(authenticatedUserId)
                .tripId(request.tripId())
                .rideId(request.rideId())
                .cancellationReason(request.cancellationReason())
                .build();

        log.info("Cancelling trip: {} for user: {}", request.tripId(), authenticatedUserId);
        String response = cancellationService.cancelTrip(effectiveRequest);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Get trip details",
            description = "Retrieve detailed information about a specific trip by ID. Used for review validation and other purposes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trip details retrieved successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.SEARCH_TRIPS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_TRIP_NOT_FOUND))
            )
    })
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponseWrapper<TripResponse>> getTrip(
            @Parameter(description = "Trip ID", example = "trip_001") @PathVariable String tripId) {
        log.info("Fetching trip details for tripId: {}", tripId);
        TripResponse response = tripService.getTrip(tripId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Tag(name = "Trip Search", description = "APIs for passengers to search for available trips based on location and route. Includes geospatial search capabilities.")
    @Operation(
            summary = "Search trips near pickup location",
            description = "Finds trips with pickup within radius. Default 5km."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Matching trips found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.SEARCH_TRIPS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameters",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_INVALID_COORDINATES))
            )
    })
    @GetMapping("/search/near-source")
    public ResponseEntity<ApiResponseWrapper<List<Trip>>> searchNearSource(
            @Parameter(description = "Latitude of the pickup location", example = "50.8154") @DecimalMin("-90.0") @DecimalMax("90.0") @RequestParam double latitude,
            @Parameter(description = "Longitude of the pickup location", example = "8.6747") @DecimalMin("-180.0") @DecimalMax("180.0") @RequestParam double longitude,
            @Parameter(description = "Search radius in kilometers (default 5.0)", example = "5.0") @DecimalMin("0.01") @RequestParam(defaultValue = "5.0") double radiusKm) {
        log.info("Searching for trips near source: ({}, {})", latitude, longitude);
        List<Trip> trips = geoService.findTripsWithSourceNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponseWrapper.success(trips));
    }

    @Tag(name = "Trip Search", description = "APIs for passengers to search for available trips based on location and route. Includes geospatial search capabilities.")
    @Operation(
            summary = "Search trips near dropoff location",
            description = "Finds trips with dropoff within radius. Default 5km."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trips found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.SEARCH_TRIPS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid coordinates or radius",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_INVALID_COORDINATES))
            )
    })
    @GetMapping("/search/near-destination")
    public ResponseEntity<ApiResponseWrapper<List<Trip>>> searchNearDestination(
            @Parameter(description = "Latitude of the dropoff location", example = "50.7265") @DecimalMin("-90.0") @DecimalMax("90.0") @RequestParam double latitude,
            @Parameter(description = "Longitude of the dropoff location", example = "7.0895") @DecimalMin("-180.0") @DecimalMax("180.0") @RequestParam double longitude,
            @Parameter(description = "Search radius in kilometers (default 5.0)", example = "5.0") @DecimalMin("0.01") @RequestParam(defaultValue = "5.0") double radiusKm) {
        log.info("Searching for trips near destination: ({}, {})", latitude, longitude);
        List<Trip> trips = geoService.findTripsWithDestinationNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponseWrapper.success(trips));
    }

    @Tag(name = "Trip Search", description = "APIs for passengers to search for available trips based on location and route. Includes geospatial search capabilities.")
    @Operation(
            summary = "Search trips matching a route",
            description = "Advanced search: finds trips matching pickup and dropoff with optional time filter."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trips found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.SEARCH_TRIPS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid coordinates or radius",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_INVALID_COORDINATES))
            )
    })
    @GetMapping("/search/matching-route")
    public ResponseEntity<ApiResponseWrapper<List<Trip>>> searchMatchingRoute(
            @RequestHeader("X-User-Id") String authenticatedUserId,
            @RequestHeader("X-User-Role") String authenticatedUserRole,
            @Parameter(description = "Latitude of pickup location", example = "50.8154") @DecimalMin("-90.0") @DecimalMax("90.0") @RequestParam double sourceLatitude,
            @Parameter(description = "Longitude of pickup location", example = "8.6747") @DecimalMin("-180.0") @DecimalMax("180.0") @RequestParam double sourceLongitude,
            @Parameter(description = "Radius around pickup in km", example = "5.0") @DecimalMin("0.01") @RequestParam(defaultValue = "5.0") double sourceRadiusKm,
            @Parameter(description = "Latitude of dropoff location", example = "50.7265") @DecimalMin("-90.0") @DecimalMax("90.0") @RequestParam double destinationLatitude,
            @Parameter(description = "Longitude of dropoff location", example = "7.0895") @DecimalMin("-180.0") @DecimalMax("180.0") @RequestParam double destinationLongitude,
            @Parameter(description = "Radius around dropoff in km", example = "5.0") @DecimalMin("0.01") @RequestParam(defaultValue = "5.0") double destinationRadiusKm,
            @Parameter(description = "Earliest departure time (ISO 8601 format, optional)", example = "2024-03-25T08:00:00Z") @RequestParam(required = false) String earliestDepartureTime,
            @Parameter(description = "Latest departure time (ISO 8601 format, optional)", example = "2024-03-25T18:00:00Z") @RequestParam(required = false) String latestDepartureTime,
            @Parameter(description = "Number of seats required", example = "2") @RequestParam(required = false) Integer requestedSeats,
            @Parameter(description = "User ID of the passenger searching", example = "jane.passenger@example.com") @RequestParam(required = false) String effectiveUserId,
            @Parameter(description = "Minimum price per seat", example = "5.0") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price per seat", example = "20.0") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Car type filter (e.g., SEDAN, SUV)", example = "SEDAN") @RequestParam(required = false) String carType,
            @Parameter(description = "Minimum available seats", example = "2") @RequestParam(required = false) Integer minAvailableSeats) {
        requirePassengerRole(authenticatedUserRole);

        if (effectiveUserId != null && !effectiveUserId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("User identity mismatch");
        }

        log.info("Searching for matching route trips");

        TripSearchCriteria criteria = new TripSearchCriteria(
                sourceLatitude, sourceLongitude, sourceRadiusKm,
                destinationLatitude, destinationLongitude, destinationRadiusKm,
                earliestDepartureTime, latestDepartureTime,
                requestedSeats, authenticatedUserId,
                minPrice, maxPrice, carType, minAvailableSeats);

        List<Trip> trips = geoService.findTripsMatchingRoute(criteria);

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
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ACTIVE_TRIPS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_TRIP_NOT_FOUND))
            )
    })
    @GetMapping("/active/driver/{driverId}")
    public ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> getDriverActiveTrips(
            @RequestHeader("X-User-Id") String authenticatedUserId,
            @RequestHeader("X-User-Role") String authenticatedUserRole,
            @Parameter(description = "Driver's user ID", example = "john.driver@example.com") @PathVariable String driverId) {
        requireDriverRole(authenticatedUserRole);
        if (!driverId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("You are not authorized to view these trips");
        }

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
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ACTIVE_TRIPS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = TripApiDoc.ERROR_TRIP_NOT_FOUND))
            )
    })
    @GetMapping("/history/driver/{driverId}")
    public ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> getDriverTripHistory(
            @RequestHeader("X-User-Id") String authenticatedUserId,
            @RequestHeader("X-User-Role") String authenticatedUserRole,
            @Parameter(description = "Driver's user ID", example = "john.driver@example.com") @PathVariable String driverId) {
        requireDriverRole(authenticatedUserRole);
        if (!driverId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("You are not authorized to view this trip history");
        }

        log.info("Fetching trip history for driver: {}", driverId);
        List<DriverTripResponse> tripHistory = activeRidesService.findDriverTripHistory(driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(tripHistory));
    }

    @Operation(
            summary = "Start a trip",
            description = "Driver starts the trip. Trip status changes from AVAILABLE to IN_PROGRESS."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trip started successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Trip already in progress or completed"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only the driver can start their own trip"
            )
    })
    @PostMapping("/{tripId}/start")
    public ResponseEntity<ApiResponseWrapper<StartTripResponse>> startTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @RequestHeader("X-User-Id") String authenticatedUserId,
            @RequestHeader("X-User-Role") String authenticatedUserRole,
            @Parameter(description = "Driver's user ID") @RequestParam(required = false) String driverId) {
        requireDriverRole(authenticatedUserRole);

        if (driverId != null && !driverId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("Driver identity mismatch");
        }

        log.info("Driver {} starting trip: {}", authenticatedUserId, tripId);
        StartTripResponse response = tripService.startTrip(tripId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response, OperationMessage.TRIP_STARTED_SUCCESS.text()));
    }

    @Operation(
            summary = "Complete a trip",
            description = "Driver ends the trip at the final destination. All remaining active rides are auto-completed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trip completed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Trip is not in progress"
            )
    })
    @PostMapping("/{tripId}/complete")
    public ResponseEntity<ApiResponseWrapper<CompleteTripResponse>> completeTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @RequestHeader("X-User-Id") String authenticatedUserId,
            @RequestHeader("X-User-Role") String authenticatedUserRole,
            @Parameter(description = "Driver's user ID") @RequestParam(required = false) String driverId) {
        requireDriverRole(authenticatedUserRole);

        if (driverId != null && !driverId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("Driver identity mismatch");
        }

        log.info("Driver {} completing trip: {}", authenticatedUserId, tripId);
        CompleteTripResponse response = completionService.completeTrip(tripId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                response,
                OperationMessage.TRIP_COMPLETED_SUCCESS.format(response.autoCompletedRides())));
    }

    private void requireDriverRole(String userRole) {
        AuthorizationHelper.requireDriverRole(userRole);
    }

    private void requirePassengerRole(String userRole) {
        AuthorizationHelper.requirePassengerRole(userRole);
    }
}
