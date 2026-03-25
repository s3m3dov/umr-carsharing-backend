package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.JoinTripService;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Management", description = "APIs for passengers to join trips, cancel bookings, and view booking history.")
public class BookingController {

    private final JoinTripService joinTripService;
    private final CancellationService cancellationService;
    private final ActiveRidesService activeRidesService;

    @Operation(
            summary = "Join a trip as passenger",
            description = "Passenger requests to join an existing trip. Validates seat availability and creates a booking."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Booking confirmed",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.JOIN_TRIP_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid booking data",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_INVALID_BOOKING_DATA))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_TRIP_NOT_FOUND))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "No available seats",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_TRIP_FULL))
            )
    })
    @PostMapping("/join")
    public ResponseEntity<ApiResponseWrapper<String>> joinTrip(
            @Valid @RequestBody JoinTripRequest request) {
        log.info("Passenger joining trip: {}", request.tripId());
        String response = joinTripService.joinTrip(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Cancel a booking",
            description = "Passenger cancels their booking on a trip."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking cancelled",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.CANCEL_BOOKING_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_BOOKING_NOT_FOUND))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot cancel (trip in progress)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_TRIP_IN_PROGRESS))
            )
    })
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponseWrapper<String>> cancelBooking(
            @Valid @RequestBody CancelTripRequest request) {
        log.info("Passenger cancelling booking: {}", request.rideId());
        String response = cancellationService.cancelRide(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Get passenger's active bookings",
            description = "Retrieves confirmed and upcoming bookings for a passenger."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active bookings retrieved",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ACTIVE_BOOKINGS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passenger not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_BOOKING_NOT_FOUND))
            )
    })
    @GetMapping("/active/passenger/{passengerId}")
    public ResponseEntity<ApiResponseWrapper<List<PassengerRideResponse>>> getPassengerActiveBookings(
            @Parameter(description = "Passenger's user ID", example = "user_456") @PathVariable String passengerId) {
        log.info("Fetching active bookings for passenger: {}", passengerId);
        List<PassengerRideResponse> activeBookings = activeRidesService.findPassengerActiveRides(passengerId);
        return ResponseEntity.ok(ApiResponseWrapper.success(activeBookings));
    }

    @Operation(
            summary = "Get passenger's ride history",
            description = "Retrieves completed and cancelled bookings for a passenger."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ride history retrieved",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ACTIVE_BOOKINGS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passenger not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_BOOKING_NOT_FOUND))
            )
    })
    @GetMapping("/history/passenger/{passengerId}")
    public ResponseEntity<ApiResponseWrapper<List<PassengerRideResponse>>> getPassengerRideHistory(
            @Parameter(description = "Passenger's user ID", example = "user_456") @PathVariable String passengerId) {
        log.info("Fetching ride history for passenger: {}", passengerId);
        List<PassengerRideResponse> rideHistory = activeRidesService.findPassengerRideHistory(passengerId);
        return ResponseEntity.ok(ApiResponseWrapper.success(rideHistory));
    }
}
