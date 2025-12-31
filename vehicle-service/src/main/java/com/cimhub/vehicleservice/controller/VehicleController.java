package com.cimhub.vehicleservice.controller;

import com.cimhub.common.dto.ApiResponse;
import com.cimhub.vehicleservice.dto.request.RegisterVehicleRequest;
import com.cimhub.vehicleservice.dto.response.VehicleResponse;
import com.cimhub.vehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Vehicle registration and management APIs")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Vehicle Service is alive"));
    }

    @Operation(summary = "Get user vehicles", description = "Retrieves all vehicles registered by a user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicles found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No vehicles found")
    })
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getUserVehicles(
            @Parameter(description = "User's unique identifier", required = true) @PathVariable String userId) {
        log.info("Getting vehicles for userId: {}", userId);
        List<VehicleResponse> resultList = vehicleService.findUserVehicles(userId);
        return ResponseEntity.ok(ApiResponse.success(resultList));
    }

    @Operation(summary = "Register vehicle", description = "Registers a new vehicle for a user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehicle registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or vehicle already registered")
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> registerVehicle(
            @Valid @RequestBody RegisterVehicleRequest request) {
        log.info("Registering vehicle for userId: {}", request.userId());
        boolean success = vehicleService.registerVehicle(request);
        if (success) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(null));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("VEHICLE_ALREADY_EXISTS", "Vehicle already registered"));
        }
    }
}