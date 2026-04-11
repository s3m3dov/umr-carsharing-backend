package com.cimhub.tripservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.tripservice.dto.examples.ReportsApiDoc;
import com.cimhub.tripservice.dto.response.*;
import com.cimhub.tripservice.service.report.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin - Reports", description = "Admin reporting endpoints")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin")
public class ReportsController {

    private final ReportsService reportsService;

    @Operation(summary = "Driver performance report", description = "Generate driver performance report")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Report generated successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = ReportsApiDoc.DRIVER_PERFORMANCE_REPORT)))
    })
    @GetMapping("/drivers")
    public ResponseEntity<ApiSuccess<DriverPerformanceReport>> driverReport(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(reportsService.getDriverPerformanceReport()));
    }

    @Operation(summary = "Bookings summary report", description = "Generate bookings summary report")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Report generated successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = ReportsApiDoc.BOOKING_SUMMARY_REPORT)))
    })
    @GetMapping("/bookings")
    public ResponseEntity<ApiSuccess<BookingSummaryReport>> bookingsReport(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(reportsService.getBookingSummaryReport()));
    }

    @Operation(summary = "Revenue report", description = "Generate revenue report")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Report generated successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = ReportsApiDoc.REVENUE_REPORT)))
    })
    @GetMapping("/revenue")
    public ResponseEntity<ApiSuccess<RevenueReport>> revenueReport(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(reportsService.getRevenueReport()));
    }

    @Operation(summary = "Passenger activity report", description = "Generate passenger activity report")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Report generated successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = ReportsApiDoc.PASSENGER_ACTIVITY_REPORT)))
    })
    @GetMapping("/passengers")
    public ResponseEntity<ApiSuccess<PassengerActivityReport>> passengerReport(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(reportsService.getPassengerActivityReport()));
    }

    @Operation(summary = "List audit logs", description = "Get paginated audit logs with optional filters")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Audit logs retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = ReportsApiDoc.AUDIT_LOGS_RESPONSE)))
    })
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiSuccess<Page<AuditLogResponse>>> listAuditLogs(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            @Parameter(description = "Filter by action") @RequestParam(required = false) AuditAction action,
            @Parameter(description = "Filter by entity type") @RequestParam(required = false) EntityType entityType,
            @Parameter(description = "Filter by actor") @RequestParam(required = false) String performedBy,
            @Parameter(description = "Filter start timestamp (ISO-8601)") @RequestParam(required = false) Instant from,
            @Parameter(description = "Filter end timestamp (ISO-8601)") @RequestParam(required = false) Instant to,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(
                ApiSuccess.success(reportsService.getAuditLogs(action, entityType, performedBy, from, to, pageable)));
    }

    @Operation(summary = "Get audit log", description = "Get a single audit log by id")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Audit log retrieved successfully",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = ReportsApiDoc.AUDIT_LOG_RESPONSE))),
        @ApiResponse(
                responseCode = "404",
                description = "Audit log not found",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = ReportsApiDoc.ERROR_AUDIT_LOG_NOT_FOUND)))
    })
    @GetMapping("/audit-logs/{id}")
    public ResponseEntity<ApiSuccess<AuditLogResponse>> getAuditLog(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String userRole,
            @Parameter(description = "Audit log id", example = "audit_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiSuccess.success(reportsService.getAuditLog(id)));
    }
}
