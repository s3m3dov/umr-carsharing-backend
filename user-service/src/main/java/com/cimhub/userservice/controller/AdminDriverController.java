package com.cimhub.userservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.examples.UserApiDoc;
import com.cimhub.userservice.dto.request.BulkDriverActionRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverDocumentResponse;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.service.AdminDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/drivers")
@RequiredArgsConstructor
@Tag(name = "Admin - Driver Management", description = "Admin endpoints for managing drivers")
@ApiResponses({
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User is not an admin",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = UserApiDoc.ERROR_ADMIN_UNAUTHORIZED))),
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = UserApiDoc.ERROR_DRIVER_NOT_FOUND)))
})
public class AdminDriverController {

    private final AdminDriverService adminDriverService;

    @Operation(summary = "List all drivers", description = "List drivers with optional status filter and pagination")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Drivers retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_LIST_DRIVERS_RESPONSE)))
    })
    @GetMapping
    public ResponseEntity<ApiSuccess<Page<DriverResponse>>> listDrivers(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(description = "Filter by driver status") @RequestParam(required = false) DriverStatus status,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        Page<DriverResponse> drivers = adminDriverService.listDrivers(status, pageable);
        return ResponseEntity.ok(ApiSuccess.success(drivers));
    }

    @Operation(summary = "Get driver detail", description = "Retrieve detailed information about a specific driver")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Driver retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_GET_DRIVER_RESPONSE)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ApiSuccess<DriverResponse>> getDriver(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(description = "Driver ID", example = "john.driver@example.com") @PathVariable String userId) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminDriverService.getDriver(userId)));
    }

    @Operation(summary = "Approve driver application", description = "Approve a pending driver application")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Driver approved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_APPROVE_DRIVER_RESPONSE)))
    })
    @PostMapping("/{userId}/approve")
    public ResponseEntity<ApiSuccess<DriverResponse>> approveDriver(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail,
            @Parameter(description = "Driver ID", example = "john.driver@example.com") @PathVariable String userId) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminDriverService.approveDriver(userId, adminEmail)));
    }

    @Operation(summary = "Reject driver application", description = "Reject a pending driver application")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Driver rejected successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_REJECT_DRIVER_RESPONSE)))
    })
    @PostMapping("/{userId}/reject")
    public ResponseEntity<ApiSuccess<DriverResponse>> rejectDriver(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail,
            @Parameter(description = "Driver ID", example = "john.driver@example.com") @PathVariable String userId) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminDriverService.rejectDriver(userId, adminEmail)));
    }

    @Operation(summary = "Bulk approve drivers", description = "Approve multiple driver applications at once")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Drivers approved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.BULK_APPROVE_RESPONSE)))
    })
    @PostMapping("/bulk-approve")
    public ResponseEntity<ApiSuccess<String>> bulkApprove(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail,
            @Valid @RequestBody BulkDriverActionRequest request) {
        AdminGuard.requireAdmin(userRole);
        int count = adminDriverService.bulkApprove(request, adminEmail);
        return ResponseEntity.ok(ApiSuccess.success(count + " drivers approved"));
    }

    @Operation(summary = "Bulk reject drivers", description = "Reject multiple driver applications at once")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Drivers rejected successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.BULK_REJECT_RESPONSE)))
    })
    @PostMapping("/bulk-reject")
    public ResponseEntity<ApiSuccess<String>> bulkReject(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail,
            @Valid @RequestBody BulkDriverActionRequest request) {
        AdminGuard.requireAdmin(userRole);
        int count = adminDriverService.bulkReject(request, adminEmail);
        return ResponseEntity.ok(ApiSuccess.success(count + " drivers rejected"));
    }

    @Operation(summary = "Edit driver info", description = "Update driver information")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Driver updated successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_UPDATE_DRIVER_RESPONSE)))
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ApiSuccess<DriverResponse>> updateDriver(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail,
            @Parameter(description = "Driver ID", example = "john.driver@example.com") @PathVariable String userId,
            @RequestBody UpdateDriverRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(
                ApiSuccess.success(adminDriverService.updateDriver(userId, request, adminEmail)));
    }

    @Operation(summary = "Delete driver", description = "Delete a driver and all associated data")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Driver deleted successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_DELETE_DRIVER_RESPONSE)))
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiSuccess<String>> deleteDriver(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail,
            @Parameter(description = "Driver ID", example = "john.driver@example.com") @PathVariable String userId) {
        AdminGuard.requireAdmin(userRole);
        adminDriverService.deleteDriver(userId, adminEmail);
        return ResponseEntity.ok(ApiSuccess.success("Driver deleted"));
    }

    @Operation(summary = "Get driver documents", description = "Retrieve driver documents (NIDA/License)")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Driver documents retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_DRIVER_DOCUMENTS_RESPONSE)))
    })
    @GetMapping("/{userId}/documents")
    public ResponseEntity<ApiSuccess<List<DriverDocumentResponse>>> getDriverDocuments(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(description = "Driver ID", example = "john.driver@example.com") @PathVariable String userId) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(adminDriverService.getDriverDocuments(userId)));
    }
}
