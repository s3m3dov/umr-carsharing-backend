package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponse;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.JoinTripService;
import com.cimhub.tripservice.service.ActiveRidesService;
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
public class BookingController {

    private final JoinTripService joinTripService;
    private final CancellationService cancellationService;
    private final ActiveRidesService activeRidesService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<String>> joinTrip(
            @Valid @RequestBody JoinTripRequest request) {
        log.info("Passenger joining trip: {}", request.tripId());
        String response = joinTripService.joinTrip(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> cancelBooking(
            @Valid @RequestBody CancelTripRequest request) {
        log.info("Passenger cancelling booking: {}", request.rideId());
        String response = cancellationService.cancelRide(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active/passenger/{passengerId}")
    public ResponseEntity<ApiResponse<List<PassengerRideResponse>>> getPassengerActiveBookings(
            @PathVariable String passengerId) {
        log.info("Fetching active bookings for passenger: {}", passengerId);
        List<PassengerRideResponse> activeBookings = activeRidesService.findPassengerActiveRides(passengerId);
        return ResponseEntity.ok(ApiResponse.success(activeBookings));
    }

    @GetMapping("/history/passenger/{passengerId}")
    public ResponseEntity<ApiResponse<List<PassengerRideResponse>>> getPassengerRideHistory(
            @PathVariable String passengerId) {
        log.info("Fetching ride history for passenger: {}", passengerId);
        List<PassengerRideResponse> rideHistory = activeRidesService.findPassengerRideHistory(passengerId);
        return ResponseEntity.ok(ApiResponse.success(rideHistory));
    }
}
