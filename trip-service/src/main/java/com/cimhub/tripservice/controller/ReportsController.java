package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.util.AdminGuard;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reports")
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
}
