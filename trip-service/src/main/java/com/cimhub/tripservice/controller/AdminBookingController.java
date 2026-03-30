package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.tripservice.dto.request.UpdateBookingRequest;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.service.AdminBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin - Booking Management", description = "Admin endpoints for managing bookings")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
@ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @Operation(summary = "List all bookings", description = "Paginated list of all bookings")
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<BookingResponse>>> listBookings(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @PageableDefault(size = 20) Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminBookingService.listBookings(pageable)));
    }

    @Operation(summary = "Get booking detail", description = "Retrieve detailed information about a specific booking")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<BookingResponse>> getBooking(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Booking ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminBookingService.getBooking(id)));
    }

    @Operation(summary = "Update booking", description = "Update booking information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<BookingResponse>> updateBooking(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Booking ID") @PathVariable String id,
            @RequestBody UpdateBookingRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                adminBookingService.updateBooking(id, request, adminEmail)));
    }

    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> cancelBooking(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Booking ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminBookingService.cancelBooking(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Booking cancelled"));
    }

    @Operation(summary = "Get bookings by passenger", description = "Retrieve all bookings for a specific passenger")
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<ApiResponseWrapper<Page<BookingResponse>>> getPassengerBookings(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Passenger ID") @PathVariable String passengerId,
            @PageableDefault(size = 20) Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                adminBookingService.getPassengerBookings(passengerId, pageable)));
    }
}
