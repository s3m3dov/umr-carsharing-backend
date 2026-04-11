package com.cimhub.tripservice.controller;

import com.cimhub.tripservice.dto.examples.BookingApiDoc;
import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.dto.response.CompleteRideResponse;
import com.cimhub.tripservice.model.OperationMessage;
import com.cimhub.tripservice.service.booking.BookingService;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.booking.CompletionService;
import com.cimhub.tripservice.service.booking.JoinTripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Management", description = "APIs for drivers to manage booking requests (accept/reject) and confirm passenger bookings for their trips.")
public class BookingController {

    private final JoinTripService joinTripService;
    private final BookingService bookingService;
    private final CancellationService cancellationService;
    private final CompletionService completionService;

    @Operation(
            summary = "Get booking details",
            description = "Retrieve detailed information about a specific booking by ID. Used for review validation and other purposes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking details retrieved successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiDoc.JOIN_TRIP_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiDoc.ERROR_BOOKING_NOT_FOUND))
            )
    })
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponseWrapper<BookingResponse>> getBooking(
            @PathVariable String bookingId) {
        log.info("Fetching booking details for bookingId: {}", bookingId);
        BookingResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Accept a booking request",
            description = "Driver accepts a passenger's booking request, confirming the ride."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking accepted",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiDoc.JOIN_TRIP_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiDoc.ERROR_BOOKING_NOT_FOUND))
            )
    })
    @PostMapping("/accept")
    public ResponseEntity<ApiResponseWrapper<String>> acceptBooking(
            @RequestParam String bookingId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Role", required = false) String authenticatedUserRole) {
        requireDriverRole(authenticatedUserRole);
        requireAuthenticatedUser(authenticatedUserId);

        log.info("Driver {} accepting booking: {}", authenticatedUserId, bookingId);
        String response = joinTripService.acceptBooking(bookingId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Reject a booking request",
            description = "Driver rejects a passenger's booking request."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking rejected",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiDoc.CANCEL_BOOKING_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiDoc.ERROR_BOOKING_NOT_FOUND))
            )
    })
    @PostMapping("/reject")
    public ResponseEntity<ApiResponseWrapper<String>> rejectBooking(
            @RequestParam String bookingId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Role", required = false) String authenticatedUserRole) {
        requireDriverRole(authenticatedUserRole);
        requireAuthenticatedUser(authenticatedUserId);

        log.info("Driver {} rejecting booking: {}", authenticatedUserId, bookingId);
        String response = joinTripService.rejectBooking(bookingId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Complete a passenger's ride",
            description = "Driver confirms a passenger's drop-off at their stop. The trip continues for remaining passengers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ride completed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ride is not in a completable state"
            )
    })
    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<ApiResponseWrapper<CompleteRideResponse>> completeRide(
            @PathVariable String bookingId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Role", required = false) String authenticatedUserRole,
            @RequestParam(required = false) String driverId) {
        requireDriverRole(authenticatedUserRole);
        requireAuthenticatedUser(authenticatedUserId);

        if (driverId != null && !driverId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("Driver identity mismatch");
        }

        log.info("Driver {} completing ride: {}", authenticatedUserId, bookingId);
        CompleteRideResponse response = completionService.completeRide(bookingId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response, OperationMessage.RIDE_COMPLETED_SUCCESS.text()));
    }

    private void requireDriverRole(String userRole) {
        AuthorizationHelper.requireDriverRole(userRole);
    }

    private void requireAuthenticatedUser(String authenticatedUserId) {
        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            throw new UnauthorizedException("Missing X-User-Id header");
        }
    }
}
