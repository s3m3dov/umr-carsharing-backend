package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.constant.HeaderNames;
import com.cimhub.common.util.OwnershipGuard;
import com.cimhub.userservice.dto.request.CreatePassengerProfileRequest;
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
import org.springframework.http.HttpStatus;
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
            @RequestHeader(HeaderNames.USER_ID) String authenticatedUserId,
            @RequestHeader(HeaderNames.USER_ROLE) UserType userRole,
            @Parameter(description = "Passenger user ID (email)", required = true) @PathVariable String userId) {
        OwnershipGuard.requireRoleOrAdmin(userRole, UserType.PASSENGER);
        OwnershipGuard.requireOwnerOrAdmin(authenticatedUserId, userId, userRole);
        log.info("Get passenger profile for userId: {}", userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(passengerService.getPassengerProfile(userId)));
    }

    @Operation(summary = "Create passenger profile", description = "Initialises the passenger sub-profile for a registered user. Call once after signup.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Passenger profile created"),
            @ApiResponse(responseCode = "404", description = "User not found or not a passenger"),
            @ApiResponse(responseCode = "409", description = "Passenger profile already exists")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> createPassengerProfile(
            @RequestHeader(HeaderNames.USER_ID) String authenticatedUserId,
            @RequestHeader(HeaderNames.USER_ROLE) UserType userRole,
            @Parameter(description = "Passenger user ID (email)", required = true) @PathVariable String userId,
            @Valid @RequestBody CreatePassengerProfileRequest request) {
        OwnershipGuard.requireRoleOrAdmin(userRole, UserType.PASSENGER);
        OwnershipGuard.requireOwnerOrAdmin(authenticatedUserId, userId, userRole);
        log.info("Create passenger profile for userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(passengerService.createPassengerProfile(userId, request)));
    }

    @Operation(summary = "Update passenger profile", description = "Updates personal info for the passenger. All fields are optional (partial update).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger profile updated"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> updatePassengerProfile(
            @RequestHeader(HeaderNames.USER_ID) String authenticatedUserId,
            @RequestHeader(HeaderNames.USER_ROLE) UserType userRole,
            @Parameter(description = "Passenger user ID (email)", required = true) @PathVariable String userId,
            @Valid @RequestBody UpdatePassengerRequest request) {
        OwnershipGuard.requireRoleOrAdmin(userRole, UserType.PASSENGER);
        OwnershipGuard.requireOwnerOrAdmin(authenticatedUserId, userId, userRole);
        log.info("Update passenger profile for userId: {}", userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(passengerService.updatePassengerProfile(userId, request)));
    }
}
