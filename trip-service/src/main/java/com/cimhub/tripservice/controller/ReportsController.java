package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.tripservice.dto.response.AuditLogResponse;
import com.cimhub.tripservice.dto.response.BookingSummaryReport;
import com.cimhub.tripservice.dto.response.DriverPerformanceReport;
import com.cimhub.tripservice.dto.response.PassengerActivityReport;
import com.cimhub.tripservice.dto.response.RevenueReport;
import com.cimhub.tripservice.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin - Reports", description = "Admin reporting endpoints")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class ReportsController {

    private final ReportsService reportsService;

    @Operation(summary = "Driver performance report", description = "Generate driver performance report")
    @GetMapping("/drivers")
    public ResponseEntity<ApiResponseWrapper<DriverPerformanceReport>> driverReport(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(reportsService.getDriverPerformanceReport()));
    }

    @Operation(summary = "Bookings summary report", description = "Generate bookings summary report")
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponseWrapper<BookingSummaryReport>> bookingsReport(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(reportsService.getBookingSummaryReport()));
    }

    @Operation(summary = "Revenue report", description = "Generate revenue report")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponseWrapper<RevenueReport>> revenueReport(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(reportsService.getRevenueReport()));
    }

    @Operation(summary = "Passenger activity report", description = "Generate passenger activity report")
    @GetMapping("/passengers")
    public ResponseEntity<ApiResponseWrapper<PassengerActivityReport>> passengerReport(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(reportsService.getPassengerActivityReport()));
    }

    @Operation(summary = "List audit logs", description = "Get paginated audit logs with optional filters")
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponseWrapper<Page<AuditLogResponse>>> listAuditLogs(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Filter by action") @RequestParam(required = false) AuditAction action,
            @Parameter(description = "Filter by entity type") @RequestParam(required = false) EntityType entityType,
            @Parameter(description = "Filter by actor") @RequestParam(required = false) String performedBy,
            @Parameter(description = "Filter start timestamp (ISO-8601)") @RequestParam(required = false) Instant from,
            @Parameter(description = "Filter end timestamp (ISO-8601)") @RequestParam(required = false) Instant to,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(
                reportsService.getAuditLogs(action, entityType, performedBy, from, to, pageable)
        ));
    }

    @Operation(summary = "Get audit log", description = "Get a single audit log by id")
    @GetMapping("/audit-logs/{id}")
    public ResponseEntity<ApiResponseWrapper<AuditLogResponse>> getAuditLog(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Audit log id") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(reportsService.getAuditLog(id)));
    }
}
