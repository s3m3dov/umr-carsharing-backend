package com.cimhub.tripservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.tripservice.dto.examples.BookingApiDoc;
import com.cimhub.tripservice.dto.request.UpdateBookingRequest;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.service.booking.AdminBookingService;
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
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin - Booking Management", description = "Admin endpoints for managing bookings")
@ApiResponses({
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User is not an admin",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = BookingApiDoc.ERROR_UNAUTHORIZED))),
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = BookingApiDoc.ERROR_BOOKING_NOT_FOUND)))
})
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @Operation(summary = "List all bookings", description = "Paginated list of all bookings")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Bookings retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = BookingApiDoc.ADMIN_LIST_BOOKINGS_RESPONSE)))
    })
    @GetMapping
    public ResponseEntity<ApiSuccess<Page<BookingResponse>>> listBookings(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminBookingService.listBookings(pageable)));
    }

    @Operation(summary = "Get booking detail", description = "Retrieve detailed information about a specific booking")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Booking retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = BookingApiDoc.ADMIN_GET_BOOKING_RESPONSE)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccess<BookingResponse>> getBooking(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            @Parameter(description = "Booking ID", example = "ride_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminBookingService.getBooking(id)));
    }

    @Operation(summary = "Update booking", description = "Update booking information")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Booking updated successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = BookingApiDoc.ADMIN_UPDATE_BOOKING_RESPONSE)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccess<BookingResponse>> updateBooking(
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
            @Parameter(description = "Booking ID", example = "ride_001") @PathVariable String id,
            @RequestBody UpdateBookingRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminBookingService.updateBooking(id, request, adminEmail)));
    }

    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Booking cancelled successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = BookingApiDoc.ADMIN_CANCEL_BOOKING_RESPONSE)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiSuccess<String>> cancelBooking(
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
            @Parameter(description = "Booking ID", example = "ride_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminBookingService.cancelBooking(id, adminEmail);
        return ResponseEntity.ok(ApiSuccess.success("Booking cancelled"));
    }

    @Operation(summary = "Get bookings by passenger", description = "Retrieve all bookings for a specific passenger")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Passenger bookings retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = BookingApiDoc.ADMIN_LIST_BOOKINGS_RESPONSE)))
    })
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<ApiSuccess<Page<BookingResponse>>> getPassengerBookings(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            @Parameter(description = "Passenger ID", example = "jane.passenger@example.com") @PathVariable
                    String passengerId,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminBookingService.getPassengerBookings(passengerId, pageable)));
    }
}
