package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.userservice.dto.request.CreateDriverProfileRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Profile", description = "Driver profile management")
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Get driver profile", description = "Returns full profile for a driver user, including license number and approval status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile returned"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<DriverResponse>> getDriverProfile(
            @Parameter(description = "Driver user ID (email)", required = true) @PathVariable String userId) {
        log.info("Get driver profile for userId: {}", userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(driverService.getDriverProfile(userId)));
    }

    @Operation(summary = "Create driver profile", description = "Initialises the driver sub-profile for a registered driver user. Requires license number. Call once after signup.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Driver profile created"),
            @ApiResponse(responseCode = "400", description = "License number is required"),
            @ApiResponse(responseCode = "404", description = "User not found or not a driver"),
            @ApiResponse(responseCode = "409", description = "Driver profile already exists")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<DriverResponse>> createDriverProfile(
            @Parameter(description = "Driver user ID (email)", required = true) @PathVariable String userId,
            @Valid @RequestBody CreateDriverProfileRequest request) {
        log.info("Create driver profile for userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(driverService.createDriverProfile(userId, request)));
    }

    @Operation(summary = "Update driver profile", description = "Updates personal info and/or license number for the driver. All fields are optional (partial update).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile updated"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<DriverResponse>> updateDriverProfile(
            @Parameter(description = "Driver user ID (email)", required = true) @PathVariable String userId,
            @Valid @RequestBody UpdateDriverRequest request) {
        log.info("Update driver profile for userId: {}", userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(driverService.updateDriverProfile(userId, request)));
    }
}
