package com.cimhub.userservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.examples.UserApiDoc;
import com.cimhub.userservice.dto.request.CreatePassengerRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.service.AdminPassengerService;
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
@RequestMapping("/api/admin/passengers")
@RequiredArgsConstructor
@Tag(name = "Admin - Passenger Management", description = "Admin endpoints for managing passengers")
@ApiResponses({
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiDoc.ERROR_ADMIN_UNAUTHORIZED))),
    @ApiResponse(responseCode = "404", description = "Resource not found",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiDoc.ERROR_PASSENGER_NOT_FOUND)))
})
public class AdminPassengerController {

    private final AdminPassengerService adminPassengerService;

    @Operation(summary = "List all passengers", description = "Paginated list of all passengers")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Passengers retrieved successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiDoc.ADMIN_LIST_PASSENGERS_RESPONSE)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<PassengerResponse>>> listPassengers(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) UserType userRole,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminPassengerService.listPassengers(pageable)));
    }

    @Operation(summary = "Get passenger detail", description = "Retrieve detailed information about a specific passenger")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Passenger retrieved successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiDoc.ADMIN_GET_PASSENGER_RESPONSE)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> getPassenger(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) UserType userRole,
            @Parameter(description = "Passenger ID", example = "jane.passenger@example.com") @PathVariable String userId) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminPassengerService.getPassenger(userId)));
    }

    @Operation(summary = "Add new passenger", description = "Deprecated: user creation must be done via auth-service signup")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Passenger created successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiDoc.ADMIN_CREATE_PASSENGER_RESPONSE)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> createPassenger(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) UserType userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true) String adminEmail,
            @Valid @RequestBody CreatePassengerRequest request) {
        AdminGuard.requireAdmin(userRole);
        PassengerResponse response = adminPassengerService.createPassenger(request, adminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseWrapper.success(response));
    }

    @Operation(summary = "Edit passenger", description = "Update passenger information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Passenger updated successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiDoc.ADMIN_UPDATE_PASSENGER_RESPONSE)))
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> updatePassenger(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) UserType userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true) String adminEmail,
            @Parameter(description = "Passenger ID", example = "jane.passenger@example.com") @PathVariable String userId,
            @RequestBody UpdatePassengerRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                adminPassengerService.updatePassenger(userId, request, adminEmail)));
    }

    @Operation(summary = "Delete passenger", description = "Delete a passenger account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Passenger deleted successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiDoc.ADMIN_DELETE_PASSENGER_RESPONSE)))
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<String>> deletePassenger(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) UserType userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false, hidden = true) @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true) String adminEmail,
            @Parameter(description = "Passenger ID", example = "jane.passenger@example.com") @PathVariable String userId) {
        AdminGuard.requireAdmin(userRole);
        adminPassengerService.deletePassenger(userId, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Passenger deleted"));
    }
}
