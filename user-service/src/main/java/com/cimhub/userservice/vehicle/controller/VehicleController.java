package com.cimhub.userservice.vehicle.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.dto.response.VehicleResponse;
import com.cimhub.userservice.vehicle.service.VehicleService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "Vehicle registration and management APIs. Allows drivers to register and manage their vehicles used for car sharing.")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponseWrapper<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponseWrapper.success("Vehicle Service is alive"));
    }

    @Operation(
            summary = "Get user vehicles",
            description = "Retrieves all vehicles registered by a user. Returns active vehicles available for sharing."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicles retrieved successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiExamples.GET_VEHICLES_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No vehicles found for user",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiExamples.ERROR_NO_VEHICLES))
            )
    })
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<List<VehicleResponse>>> getUserVehicles(
            @Parameter(description = "User's unique identifier", required = true, example = "john.driver@example.com")
            @PathVariable String userId) {
        log.info("Getting vehicles for userId: {}", userId);
        List<VehicleResponse> resultList = vehicleService.findUserVehicles(userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(resultList));
    }

    @Operation(
            summary = "Register vehicle",
            description = "Registers a new vehicle for a driver. Validates license plate uniqueness. Maximum 5 vehicles per driver."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Vehicle registered successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiExamples.REGISTER_VEHICLE_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiExamples.ERROR_INVALID_VEHICLE_DATA))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Vehicle already exists or limit exceeded",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VehicleApiExamples.ERROR_VEHICLE_LIMIT))
            )
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<VehicleResponse>> registerVehicle(
            @Valid @RequestBody RegisterVehicleRequest request) {
        log.info("Registering vehicle for userId: {}", request.userId());
        VehicleResponse registeredVehicle = vehicleService.registerVehicle(request);
        if (registeredVehicle != null) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponseWrapper.success(registeredVehicle));
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponseWrapper.error("VEHICLE_ALREADY_EXISTS", "Vehicle already registered"));
        }
    }
}
