package com.cimhub.userservice.vehicle.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.constant.HeaderNames;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.request.UpdateVehicleRequest;
import com.cimhub.userservice.dto.response.AdminVehicleResponse;
import com.cimhub.userservice.vehicle.dto.examples.VehicleApiDoc;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.service.AdminVehicleService;
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
@ApiResponses({
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiDoc.ERROR_ADMIN_UNAUTHORIZED))),
    @ApiResponse(responseCode = "404", description = "Resource not found",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiDoc.ERROR_VEHICLE_NOT_FOUND)))
})
public class AdminVehicleController {

    private final AdminVehicleService adminVehicleService;

    @Operation(summary = "List all vehicles", description = "Paginated list of all vehicles")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiDoc.ADMIN_LIST_VEHICLES_RESPONSE)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<AdminVehicleResponse>>> listVehicles(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_ROLE, required = false) UserType userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminVehicleService.listVehicles(pageable)));
    }

    @Operation(summary = "Get vehicle detail", description = "Retrieve detailed information about a specific vehicle")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiDoc.ADMIN_GET_VEHICLE_RESPONSE)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<AdminVehicleResponse>> getVehicle(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_ROLE, required = false) UserType userRole,
            @Parameter(description = "Vehicle ID", example = "vehicle_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminVehicleService.getVehicle(id)));
    }

    @Operation(summary = "Add vehicle", description = "Register a new vehicle in the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vehicle created successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiDoc.ADMIN_CREATE_VEHICLE_RESPONSE)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<AdminVehicleResponse>> createVehicle(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_ROLE, required = false) UserType userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_EMAIL, required = false) String adminEmail,
            @Valid @RequestBody RegisterVehicleRequest request) {
        AdminGuard.requireAdmin(userRole);
        AdminVehicleResponse response = adminVehicleService.createVehicle(request, adminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseWrapper.success(response));
    }

    @Operation(summary = "Update vehicle", description = "Update vehicle information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehicle updated successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiDoc.ADMIN_UPDATE_VEHICLE_RESPONSE)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<AdminVehicleResponse>> updateVehicle(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_ROLE, required = false) UserType userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_EMAIL, required = false) String adminEmail,
            @Parameter(description = "Vehicle ID", example = "vehicle_001") @PathVariable String id,
            @RequestBody UpdateVehicleRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                adminVehicleService.updateVehicle(id, request, adminEmail)));
    }

    @Operation(summary = "Delete vehicle", description = "Delete a vehicle from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehicle deleted successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiDoc.ADMIN_DELETE_VEHICLE_RESPONSE)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> deleteVehicle(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_ROLE, required = false) UserType userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderNames.USER_EMAIL, required = false) String adminEmail,
            @Parameter(description = "Vehicle ID", example = "vehicle_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminVehicleService.deleteVehicle(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Vehicle deleted"));
    }
}
