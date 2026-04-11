package com.cimhub.tripservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.examples.BookingApiDoc;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.booking.JoinTripService;
import com.cimhub.tripservice.service.trip.ActiveRidesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Ride Management",
        description =
                "APIs for passengers to book rides (join trips), cancel rides, and view ride history. A ride represents a passenger's segment within a trip.")
public class RideController {

    private final JoinTripService joinTripService;
    private final CancellationService cancellationService;
    private final ActiveRidesService activeRidesService;

    @Operation(
            summary = "Book a ride (join a trip)",
            description =
                    "Passenger books a seat on an existing trip. Creates a ride - the passenger's segment within the trip.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Ride booked successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.JOIN_TRIP_RESPONSE))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid ride data",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_INVALID_BOOKING_DATA))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Trip not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_TRIP_NOT_FOUND))),
                @ApiResponse(
                        responseCode = "409",
                        description = "No available seats",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_TRIP_FULL)))
            })
    @PostMapping("/book")
    public ResponseEntity<ApiSuccess<String>> bookRide(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            @Valid @RequestBody JoinTripRequest request) {
        requirePassengerRole(authenticatedUserRole);

        log.info("Passenger {} booking ride on trip: {}", authenticatedUserId, request.tripId());
        String response = joinTripService.joinTrip(authenticatedUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.success(response));
    }

    @Operation(
            summary = "Book a ride with driver approval (join a trip)",
            description =
                    "Passenger requests a seat on an existing trip. The booking starts in REQUESTED state and must be explicitly accepted or rejected by the driver.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Booking request submitted, awaiting driver confirmation",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.JOIN_TRIP_RESPONSE))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid ride data",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_INVALID_BOOKING_DATA))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Trip not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_TRIP_NOT_FOUND))),
                @ApiResponse(
                        responseCode = "409",
                        description = "No available seats",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_TRIP_FULL)))
            })
    @PostMapping("/book/new")
    public ResponseEntity<ApiSuccess<String>> bookRideWithDriverAccept(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            @Valid @RequestBody JoinTripRequest request) {
        requirePassengerRole(authenticatedUserRole);

        log.info("Passenger {} requesting ride (driver-accept) on trip: {}", authenticatedUserId, request.tripId());
        String response = joinTripService.joinTripWithDriverAccept(authenticatedUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.success(response));
    }

    @Operation(summary = "Cancel a ride", description = "Passenger cancels their ride on a trip.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ride cancelled",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.CANCEL_BOOKING_RESPONSE))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Ride not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_BOOKING_NOT_FOUND))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Cannot cancel (trip in progress)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_TRIP_IN_PROGRESS)))
            })
    @PostMapping("/cancel")
    public ResponseEntity<ApiSuccess<String>> cancelRide(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            @Valid @RequestBody CancelTripRequest request) {
        requirePassengerRole(authenticatedUserRole);

        log.info("Passenger {} cancelling ride: {}", authenticatedUserId, request.rideId());
        String response = cancellationService.cancelRide(authenticatedUserId, request);
        return ResponseEntity.ok(ApiSuccess.success(response));
    }

    @Operation(
            summary = "Get passenger's active rides",
            description = "Retrieves confirmed and upcoming rides for a passenger.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Active rides retrieved",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ACTIVE_BOOKINGS_RESPONSE))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Passenger not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_BOOKING_NOT_FOUND)))
            })
    @GetMapping("/active/passenger/{passengerId}")
    public ResponseEntity<ApiSuccess<List<PassengerRideResponse>>> getPassengerActiveRides(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            @Parameter(description = "Passenger's user ID", example = "jane.passenger@example.com") @PathVariable
                    String passengerId) {
        requirePassengerRole(authenticatedUserRole);

        if (!passengerId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("You are not authorized to view these rides");
        }

        log.info("Fetching active rides for passenger: {}", passengerId);
        List<PassengerRideResponse> activeRides = activeRidesService.findPassengerActiveRides(passengerId);
        return ResponseEntity.ok(ApiSuccess.success(activeRides));
    }

    @Operation(
            summary = "Get passenger's ride history",
            description = "Retrieves completed and cancelled rides for a passenger.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ride history retrieved",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ACTIVE_BOOKINGS_RESPONSE))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Passenger not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = BookingApiDoc.ERROR_BOOKING_NOT_FOUND)))
            })
    @GetMapping("/history/passenger/{passengerId}")
    public ResponseEntity<ApiSuccess<List<PassengerRideResponse>>> getPassengerRideHistory(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            @Parameter(description = "Passenger's user ID", example = "jane.passenger@example.com") @PathVariable
                    String passengerId) {
        requirePassengerRole(authenticatedUserRole);

        if (!passengerId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("You are not authorized to view this ride history");
        }

        log.info("Fetching ride history for passenger: {}", passengerId);
        List<PassengerRideResponse> rideHistory = activeRidesService.findPassengerRideHistory(passengerId);
        return ResponseEntity.ok(ApiSuccess.success(rideHistory));
    }

    private void requirePassengerRole(String userRole) {
        AuthorizationHelper.requirePassengerRole(userRole);
    }
}
