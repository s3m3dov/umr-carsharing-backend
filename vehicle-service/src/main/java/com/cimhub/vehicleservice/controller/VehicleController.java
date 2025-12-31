package com.cimhub.vehicleservice.controller;

import com.cimhub.common.dto.ApiResponse;
import com.cimhub.vehicleservice.dto.VehicleRegisterRequestDTO;
import com.cimhub.vehicleservice.dto.VehicleResponseDTO;
import com.cimhub.vehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Vehicle registration and management APIs")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

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
    public ResponseEntity<ApiResponse<List<VehicleResponseDTO>>> getUserVehicles(
            @Parameter(description = "User's unique identifier", required = true) @PathVariable String userId) {
        log.info("Getting vehicles for userId: {}", userId);
        List<VehicleResponseDTO> resultList = vehicleService.getUserVehicles(userId);
        return ResponseEntity.ok(ApiResponse.success(resultList));
    }

    @Operation(summary = "Register vehicle", description = "Registers a new vehicle for a user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehicle registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or vehicle already registered")
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> registerVehicle(
            @Valid @RequestBody VehicleRegisterRequestDTO requestInfo) {
        log.info("Registering vehicle for userId: {}", requestInfo.getUserId());
        vehicleService.addNewVehicle(requestInfo.getUserId(), requestInfo);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
    }
}