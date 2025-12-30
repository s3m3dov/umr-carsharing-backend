package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.RequestDTO;
import com.cimhub.common.dto.ResponseDTO;
import com.cimhub.tripservice.dto.CancelRequestDTO;
import com.cimhub.tripservice.dto.JoinTripRequestDTO;
import com.cimhub.tripservice.dto.PassengerUpcomingRideDTO;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.JoinTripService;
import com.cimhub.tripservice.service.UpcomingRidesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consolidated controller for Booking management (passenger-focused operations)
 * Handles: joining trips, cancelling bookings, viewing upcoming bookings
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final JoinTripService joinTripService;
    private final CancellationService cancellationService;
    private final UpcomingRidesService upcomingRidesService;

    // ==================== Booking Creation ====================

    /**
     * Passenger joins a trip (creates a booking)
     * POST /api/bookings/join
     */
    @PostMapping("/join")
    public ResponseEntity<ResponseDTO<String>> joinTrip(
            @RequestBody RequestDTO<JoinTripRequestDTO> request) {

        ResponseDTO<String> response = joinTripService.joinTrip(request);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== Booking Cancellation ====================

    /**
     * Passenger cancels their booking
     * POST /api/bookings/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<ResponseDTO<String>> cancelBooking(
            @RequestBody RequestDTO<CancelRequestDTO> request) {

        ResponseDTO<String> response = cancellationService.cancelRide(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== Passenger's Upcoming Bookings ====================

    /**
     * Get passenger's upcoming bookings
     * GET /api/bookings/upcoming/passenger/{passengerId}
     */
    @GetMapping("/upcoming/passenger/{passengerId}")
    public ResponseEntity<List<PassengerUpcomingRideDTO>> getPassengerUpcomingBookings(
            @PathVariable String passengerId) {

        List<PassengerUpcomingRideDTO> upcomingBookings = upcomingRidesService.getPassengerUpcomingRides(passengerId);
        return ResponseEntity.ok(upcomingBookings);
    }
}
