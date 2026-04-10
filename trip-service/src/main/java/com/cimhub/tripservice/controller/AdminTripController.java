package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.service.trip.AdminTripService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/trips")
@RequiredArgsConstructor
@Tag(name = "Admin - Trip Management", description = "Admin endpoints for managing trips")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
@ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class AdminTripController {

    private final AdminTripService adminTripService;

    @Operation(summary = "List all trips", description = "Paginated list of all trips")
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<AdminTripResponse>>> listTrips(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.listTrips(pageable)));
    }

    @Operation(summary = "Get trip detail", description = "Retrieve detailed information about a specific trip")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<AdminTripResponse>> getTrip(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Trip ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.getTrip(id)));
    }

    @Operation(summary = "Get upcoming trips", description = "Retrieve upcoming trips scheduled in the future")
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponseWrapper<Page<AdminTripResponse>>> getUpcomingTrips(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.getUpcomingTrips(pageable)));
    }

    @Operation(summary = "Get trip history", description = "Retrieve historical/completed trips")
    @GetMapping("/history")
    public ResponseEntity<ApiResponseWrapper<Page<AdminTripResponse>>> getTripHistory(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.getTripHistory(pageable)));
    }

    @Operation(summary = "Cancel/delete trip", description = "Cancel or delete a trip from the system")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> cancelTrip(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Trip ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminTripService.cancelTrip(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Trip cancelled"));
    }
}
