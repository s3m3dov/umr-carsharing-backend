package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.request.BulkDriverActionRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverDocumentResponse;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.service.AdminDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/drivers")
@RequiredArgsConstructor
@Tag(name = "Admin - Driver Management", description = "Admin endpoints for managing drivers")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
@ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class AdminDriverController {

    private final AdminDriverService adminDriverService;

    @Operation(summary = "List all drivers", description = "List drivers with optional status filter and pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved drivers list")
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<DriverResponse>>> listDrivers(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @RequestParam(required = false) DriverStatus status,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        Page<DriverResponse> drivers = adminDriverService.listDrivers(status, pageable);
        return ResponseEntity.ok(ApiResponseWrapper.success(drivers));
    }

    @Operation(summary = "Get driver detail", description = "Retrieve detailed information about a specific driver")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved driver")
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    @ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<DriverResponse>> getDriver(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Driver ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminDriverService.getDriver(id)));
    }

    @Operation(summary = "Approve driver application", description = "Approve a pending driver application")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponseWrapper<DriverResponse>> approveDriver(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Driver ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminDriverService.approveDriver(id, adminEmail)));
    }

    @Operation(summary = "Reject driver application", description = "Reject a pending driver application")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponseWrapper<DriverResponse>> rejectDriver(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Driver ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminDriverService.rejectDriver(id, adminEmail)));
    }

    @Operation(summary = "Bulk approve drivers", description = "Approve multiple driver applications at once")
    @PostMapping("/bulk-approve")
    public ResponseEntity<ApiResponseWrapper<String>> bulkApprove(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Valid @RequestBody BulkDriverActionRequest request) {
        AdminGuard.requireAdmin(userRole);
        int count = adminDriverService.bulkApprove(request, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success(count + " drivers approved"));
    }

    @Operation(summary = "Bulk reject drivers", description = "Reject multiple driver applications at once")
    @PostMapping("/bulk-reject")
    public ResponseEntity<ApiResponseWrapper<String>> bulkReject(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Valid @RequestBody BulkDriverActionRequest request) {
        AdminGuard.requireAdmin(userRole);
        int count = adminDriverService.bulkReject(request, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success(count + " drivers rejected"));
    }

    @Operation(summary = "Edit driver info", description = "Update driver information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<DriverResponse>> updateDriver(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Driver ID") @PathVariable String id,
            @RequestBody UpdateDriverRequest request) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminDriverService.updateDriver(id, request, adminEmail)));
    }

    @Operation(summary = "Delete driver", description = "Delete a driver and all associated data")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> deleteDriver(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Driver ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminDriverService.deleteDriver(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Driver deleted"));
    }

    @Operation(summary = "Get driver documents", description = "Retrieve driver documents (NIDA/License)")
    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponseWrapper<List<DriverDocumentResponse>>> getDriverDocuments(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Driver ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminDriverService.getDriverDocuments(id)));
    }
}
