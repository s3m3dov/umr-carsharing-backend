package com.cimhub.userservice.vehicle.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.request.UpdateVehicleRequest;
import com.cimhub.userservice.dto.response.AdminVehicleResponse;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.service.AdminVehicleService;
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
@RequestMapping("/api/admin/vehicles")
@RequiredArgsConstructor
@Tag(name = "Admin - Vehicle Management", description = "Admin endpoints for managing vehicles")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
@ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class AdminVehicleController {

    private final AdminVehicleService adminVehicleService;

    @Operation(summary = "List all vehicles", description = "Paginated list of all vehicles")
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<AdminVehicleResponse>>> listVehicles(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminVehicleService.listVehicles(pageable)));
    }

    @Operation(summary = "Get vehicle detail", description = "Retrieve detailed information about a specific vehicle")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<AdminVehicleResponse>> getVehicle(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Vehicle ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminVehicleService.getVehicle(id)));
    }

    @Operation(summary = "Add vehicle", description = "Register a new vehicle in the system")
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<AdminVehicleResponse>> createVehicle(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Valid @RequestBody RegisterVehicleRequest request) {
        AdminGuard.requireAdmin(userRole);
        AdminVehicleResponse response = adminVehicleService.createVehicle(request, adminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseWrapper.success(response));
    }

    @Operation(summary = "Update vehicle", description = "Update vehicle information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<AdminVehicleResponse>> updateVehicle(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Vehicle ID") @PathVariable String id,
            @RequestBody UpdateVehicleRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                adminVehicleService.updateVehicle(id, request, adminEmail)));
    }

    @Operation(summary = "Delete vehicle", description = "Delete a vehicle from the system")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> deleteVehicle(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Vehicle ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminVehicleService.deleteVehicle(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Vehicle deleted"));
    }
}
