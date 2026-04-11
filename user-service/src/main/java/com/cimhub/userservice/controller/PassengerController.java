package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.util.OwnershipGuard;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.service.PassengerService;
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
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
@Tag(name = "Passenger Profile", description = "Passenger profile management")
public class PassengerController {

    private final PassengerService passengerService;

    @Operation(summary = "Get passenger profile", description = "Returns full profile for a passenger user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger profile returned"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> getPassengerProfile(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ID, required = true) String authenticatedUserId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) UserType userRole,
            @Parameter(description = "Passenger user ID (email)", required = true) @PathVariable String userId) {
        OwnershipGuard.requireRoleOrAdmin(userRole, UserType.PASSENGER);
        OwnershipGuard.requireOwnerOrAdmin(authenticatedUserId, userId, userRole);
        log.info("Get passenger profile for userId: {}", userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(passengerService.getPassengerProfile(userId)));
    }

    @Operation(summary = "Update passenger profile", description = "Updates personal info for the passenger. All fields are optional (partial update).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger profile updated"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> updatePassengerProfile(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ID, required = true) String authenticatedUserId,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) UserType userRole,
            @Parameter(description = "Passenger user ID (email)", required = true) @PathVariable String userId,
            @Valid @RequestBody UpdatePassengerRequest request) {
        OwnershipGuard.requireRoleOrAdmin(userRole, UserType.PASSENGER);
        OwnershipGuard.requireOwnerOrAdmin(authenticatedUserId, userId, userRole);
        log.info("Update passenger profile for userId: {}", userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(passengerService.updatePassengerProfile(userId, request)));
    }
}
