package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.CompletionService;
import com.cimhub.tripservice.service.JoinTripService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Management", description = "APIs for drivers to manage booking requests (accept/reject) and confirm passenger bookings for their trips.")
public class BookingController {

    private final JoinTripService joinTripService;
    private final CancellationService cancellationService;
    private final CompletionService completionService;

    @Operation(
            summary = "Accept a booking request",
            description = "Driver accepts a passenger's booking request, confirming the ride."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking accepted",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.JOIN_TRIP_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_BOOKING_NOT_FOUND))
            )
    })
    @PostMapping("/accept")
    public ResponseEntity<ApiResponseWrapper<String>> acceptBooking(
            @RequestParam String bookingId) {
        log.info("Driver accepting booking: {}", bookingId);
        String response = joinTripService.acceptBooking(bookingId);
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
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.CANCEL_BOOKING_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = BookingApiExamples.ERROR_BOOKING_NOT_FOUND))
            )
    })
    @PostMapping("/reject")
    public ResponseEntity<ApiResponseWrapper<String>> rejectBooking(
            @RequestParam String bookingId) {
        log.info("Driver rejecting booking: {}", bookingId);
        String response = joinTripService.rejectBooking(bookingId);
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
    public ResponseEntity<ApiResponseWrapper<String>> completeRide(
            @PathVariable String bookingId,
            @RequestParam String driverId) {
        log.info("Driver {} completing ride: {}", driverId, bookingId);
        String response = completionService.completeRide(bookingId, driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}