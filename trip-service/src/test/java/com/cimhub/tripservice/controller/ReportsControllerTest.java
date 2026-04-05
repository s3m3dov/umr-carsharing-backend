package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.tripservice.dto.response.AuditLogResponse;
import com.cimhub.tripservice.dto.response.BookingSummaryReport;
import com.cimhub.tripservice.dto.response.DriverPerformanceReport;
import com.cimhub.tripservice.dto.response.PassengerActivityReport;
import com.cimhub.tripservice.dto.response.RevenueReport;
import com.cimhub.tripservice.service.ReportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportsControllerTest {

    @Mock
    private ReportsService reportsService;

    private ReportsController reportsController;

    @BeforeEach
    void setUp() {
        reportsController = new ReportsController(reportsService);
    }

    @Test
    void reportEndpoints_withAdminRole_shouldReturnReports() {
        DriverPerformanceReport drivers = DriverPerformanceReport.builder().totalDrivers(5).build();
        BookingSummaryReport bookings = BookingSummaryReport.builder().totalBookings(10).build();
        RevenueReport revenue = RevenueReport.builder().totalRevenue(120.0).build();
        PassengerActivityReport passengers = PassengerActivityReport.builder().totalBookings(8).build();

        when(reportsService.getDriverPerformanceReport()).thenReturn(drivers);
        when(reportsService.getBookingSummaryReport()).thenReturn(bookings);
        when(reportsService.getRevenueReport()).thenReturn(revenue);
        when(reportsService.getPassengerActivityReport()).thenReturn(passengers);

        ResponseEntity<ApiResponseWrapper<DriverPerformanceReport>> r1 = reportsController.driverReport("ADMIN");
        ResponseEntity<ApiResponseWrapper<BookingSummaryReport>> r2 = reportsController.bookingsReport("admin");
        ResponseEntity<ApiResponseWrapper<RevenueReport>> r3 = reportsController.revenueReport("ADMIN");
        ResponseEntity<ApiResponseWrapper<PassengerActivityReport>> r4 = reportsController.passengerReport("ADMIN");

        assertEquals(HttpStatus.OK, r1.getStatusCode());
        assertEquals(5, r1.getBody().getData().totalDrivers());
        assertEquals(10, r2.getBody().getData().totalBookings());
        assertEquals(120.0, r3.getBody().getData().totalRevenue());
        assertEquals(8, r4.getBody().getData().totalBookings());
    }

    @Test
    void auditLogEndpoints_withAdminRole_shouldReturnData() {
        Pageable pageable = PageRequest.of(0, 5);
        AuditLogResponse log = AuditLogResponse.builder().id("a-1").action(AuditAction.BOOK).build();
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log), pageable, 1);

        when(reportsService.getAuditLogs(
                AuditAction.BOOK,
                EntityType.BOOKING,
                "user-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                pageable)).thenReturn(page);
        when(reportsService.getAuditLog("a-1")).thenReturn(log);

        ResponseEntity<ApiResponseWrapper<Page<AuditLogResponse>>> listResponse = reportsController.listAuditLogs(
                "ADMIN",
                AuditAction.BOOK,
                EntityType.BOOKING,
                "user-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                pageable);
        ResponseEntity<ApiResponseWrapper<AuditLogResponse>> getResponse = reportsController.getAuditLog("ADMIN", "a-1");

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(1, listResponse.getBody().getData().getTotalElements());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("a-1", getResponse.getBody().getData().id());

        verify(reportsService).getAuditLog("a-1");
    }

    @Test
    void endpoints_withNonAdminRole_shouldThrowForbidden() {
        Pageable pageable = PageRequest.of(0, 1);
        assertThrows(ForbiddenException.class, () -> reportsController.driverReport("PASSENGER"));
        assertThrows(ForbiddenException.class, () -> reportsController.bookingsReport("DRIVER"));
        assertThrows(ForbiddenException.class, () -> reportsController.revenueReport("guest"));
        assertThrows(ForbiddenException.class, () -> reportsController.passengerReport("USER"));
        assertThrows(ForbiddenException.class, () -> reportsController.listAuditLogs(
                "DRIVER", null, null, null, null, null, pageable));
        assertThrows(ForbiddenException.class, () -> reportsController.getAuditLog("PASSENGER", "a-1"));
    }
}
