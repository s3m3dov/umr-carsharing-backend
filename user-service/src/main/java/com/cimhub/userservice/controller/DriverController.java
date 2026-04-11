package com.cimhub.userservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.util.OwnershipGuard;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Profile", description = "Driver profile management")
public class DriverController {

    private final DriverService driverService;

    @Operation(
            summary = "Get driver profile",
            description = "Returns full profile for a driver user, including license number and approval status")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Driver profile returned"),
                @ApiResponse(responseCode = "404", description = "Driver not found")
            })
    @GetMapping("/{userId}")
    public ResponseEntity<ApiSuccess<DriverResponse>> getDriverProfile(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(description = "Driver user ID (email)", required = true) @PathVariable String userId) {
        OwnershipGuard.requireRoleOrAdmin(userRole, UserType.DRIVER);
        OwnershipGuard.requireOwnerOrAdmin(authenticatedUserId, userId, userRole);
        log.info("Get driver profile for userId: {}", userId);
        return ResponseEntity.ok(ApiSuccess.success(driverService.getDriverProfile(userId)));
    }

    @Operation(
            summary = "Update driver profile",
            description =
                    "Updates personal info and/or license number for the driver. All fields are optional (partial update).")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Driver profile updated"),
                @ApiResponse(responseCode = "404", description = "Driver not found")
            })
    @PutMapping("/{userId}")
    public ResponseEntity<ApiSuccess<DriverResponse>> updateDriverProfile(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(description = "Driver user ID (email)", required = true) @PathVariable String userId,
            @Valid @RequestBody UpdateDriverRequest request) {
        OwnershipGuard.requireRoleOrAdmin(userRole, UserType.DRIVER);
        OwnershipGuard.requireOwnerOrAdmin(authenticatedUserId, userId, userRole);
        log.info("Update driver profile for userId: {}", userId);
        return ResponseEntity.ok(ApiSuccess.success(driverService.updateDriverProfile(userId, request)));
    }
}
