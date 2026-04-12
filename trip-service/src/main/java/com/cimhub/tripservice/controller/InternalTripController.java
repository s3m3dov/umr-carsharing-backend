package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.tripservice.service.trip.InternalTripUserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for user management in trip-service.
 * Hidden from public API documentation.
 */
@Slf4j
@Hidden
@RestController
@RequestMapping("/api/internal/trips/users")
@RequiredArgsConstructor
public class InternalTripController {

    private final InternalTripUserService internalUserService;

    @Operation(summary = "Internal: Delete user data", description = "Deletes all user-related data from trip-service")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiSuccess<String>> deleteUserData(@PathVariable String userId) {
        log.info("Internal request to delete all data for user: {}", userId);
        internalUserService.deleteUserData(userId);
        return ResponseEntity.ok(ApiSuccess.success("User data deleted from trip-service"));
    }
}
