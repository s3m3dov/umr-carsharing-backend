package com.cimhub.tripservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.tripservice.dto.examples.TripApiDoc;
import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.service.trip.AdminTripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@ApiResponses({
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User is not an admin",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = TripApiDoc.ERROR_UNAUTHORIZED))),
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = TripApiDoc.ERROR_TRIP_NOT_FOUND)))
})
public class AdminTripController {

    private final AdminTripService adminTripService;

    @Operation(summary = "List all trips", description = "Paginated list of all trips")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Trips retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = TripApiDoc.ADMIN_LIST_TRIPS_RESPONSE)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<AdminTripResponse>>> listTrips(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.listTrips(pageable)));
    }

    @Operation(summary = "Get trip detail", description = "Retrieve detailed information about a specific trip")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Trip retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = TripApiDoc.ADMIN_GET_TRIP_RESPONSE)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<AdminTripResponse>> getTrip(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            @Parameter(description = "Trip ID", example = "trip_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.getTrip(id)));
    }

    @Operation(summary = "Get upcoming trips", description = "Retrieve upcoming trips scheduled in the future")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Upcoming trips retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = TripApiDoc.ADMIN_LIST_TRIPS_RESPONSE)))
    })
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponseWrapper<Page<AdminTripResponse>>> getUpcomingTrips(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.getUpcomingTrips(pageable)));
    }

    @Operation(summary = "Get trip history", description = "Retrieve historical/completed trips")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Trip history retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = TripApiDoc.ADMIN_LIST_TRIPS_RESPONSE)))
    })
    @GetMapping("/history")
    public ResponseEntity<ApiResponseWrapper<Page<AdminTripResponse>>> getTripHistory(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminTripService.getTripHistory(pageable)));
    }

    @Operation(summary = "Cancel/delete trip", description = "Cancel or delete a trip from the system")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Trip cancelled successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = TripApiDoc.ADMIN_CANCEL_TRIP_RESPONSE))),
        @ApiResponse(
                responseCode = "409",
                description = "Trip already cancelled",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = TripApiDoc.ADMIN_CANCEL_TRIP_ALREADY_CANCELLED)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> cancelTrip(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail,
            @Parameter(description = "Trip ID", example = "trip_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminTripService.cancelTrip(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Trip cancelled"));
    }
}
