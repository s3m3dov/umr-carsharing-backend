package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.request.CreatePassengerRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.service.AdminPassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/passengers")
@RequiredArgsConstructor
@Tag(name = "Admin - Passenger Management", description = "Admin endpoints for managing passengers")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
@ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class AdminPassengerController {

    private final AdminPassengerService adminPassengerService;

    @Operation(summary = "List all passengers", description = "Paginated list of all passengers")
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<PassengerResponse>>> listPassengers(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminPassengerService.listPassengers(pageable)));
    }

    @Operation(summary = "Get passenger detail", description = "Retrieve detailed information about a specific passenger")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> getPassenger(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Passenger ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminPassengerService.getPassenger(id)));
    }

    @Operation(summary = "Add new passenger", description = "Deprecated: user creation must be done via auth-service signup")
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> createPassenger(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Valid @RequestBody CreatePassengerRequest request) {
        AdminGuard.requireAdmin(userRole);
        PassengerResponse response = adminPassengerService.createPassenger(request, adminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseWrapper.success(response));
    }

    @Operation(summary = "Edit passenger", description = "Update passenger information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> updatePassenger(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Passenger ID") @PathVariable String id,
            @RequestBody UpdatePassengerRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                adminPassengerService.updatePassenger(id, request, adminEmail)));
    }

    @Operation(summary = "Delete passenger", description = "Delete a passenger account")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> deletePassenger(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Passenger ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminPassengerService.deletePassenger(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Passenger deleted"));
    }
}
