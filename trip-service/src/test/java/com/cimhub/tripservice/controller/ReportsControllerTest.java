package com.cimhub.tripservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.tripservice.dto.response.AuditLogResponse;
import com.cimhub.tripservice.dto.response.BookingSummaryReport;
import com.cimhub.tripservice.dto.response.DriverPerformanceReport;
import com.cimhub.tripservice.dto.response.PassengerActivityReport;
import com.cimhub.tripservice.dto.response.RevenueReport;
import com.cimhub.tripservice.service.report.ReportsService;
import java.time.Instant;
import java.util.List;
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
        DriverPerformanceReport drivers =
                DriverPerformanceReport.builder().totalDrivers(5).build();
        BookingSummaryReport bookings =
                BookingSummaryReport.builder().totalBookings(10).build();
        RevenueReport revenue = RevenueReport.builder().totalRevenue(120.0).build();
        PassengerActivityReport passengers =
                PassengerActivityReport.builder().totalBookings(8).build();

        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-02T00:00:00Z");

        when(reportsService.getDriverPerformanceReport(from, to)).thenReturn(drivers);
        when(reportsService.getBookingSummaryReport(from, to)).thenReturn(bookings);
        when(reportsService.getRevenueReport(from, to)).thenReturn(revenue);
        when(reportsService.getPassengerActivityReport(from, to)).thenReturn(passengers);

        ResponseEntity<ApiSuccess<DriverPerformanceReport>> r1 = reportsController.driverReport("ADMIN", from, to);
        ResponseEntity<ApiSuccess<BookingSummaryReport>> r2 = reportsController.bookingsReport("admin", from, to);
        ResponseEntity<ApiSuccess<RevenueReport>> r3 = reportsController.revenueReport("ADMIN", from, to);
        ResponseEntity<ApiSuccess<PassengerActivityReport>> r4 = reportsController.passengerReport("ADMIN", from, to);

        assertEquals(HttpStatus.OK, r1.getStatusCode());
        assertEquals(5, r1.getBody().getData().totalDrivers());
        assertEquals(10, r2.getBody().getData().totalBookings());
        assertEquals(120.0, r3.getBody().getData().totalRevenue());
        assertEquals(8, r4.getBody().getData().totalBookings());
    }

    @Test
    void reportEndpoints_withoutDates_shouldReturnReports() {
        DriverPerformanceReport drivers =
                DriverPerformanceReport.builder().totalDrivers(5).build();

        when(reportsService.getDriverPerformanceReport(null, null)).thenReturn(drivers);

        ResponseEntity<ApiSuccess<DriverPerformanceReport>> r1 = reportsController.driverReport("ADMIN", null, null);

        assertEquals(HttpStatus.OK, r1.getStatusCode());
        assertEquals(5, r1.getBody().getData().totalDrivers());
    }

    @Test
    void auditLogEndpoints_withAdminRole_shouldReturnData() {
        Pageable pageable = PageRequest.of(0, 5);
        AuditLogResponse log =
                AuditLogResponse.builder().id("a-1").action(AuditAction.BOOK).build();
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log), pageable, 1);

        when(reportsService.getAuditLogs(
                        AuditAction.BOOK,
                        EntityType.BOOKING,
                        "user-1",
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-02T00:00:00Z"),
                        pageable))
                .thenReturn(page);
        when(reportsService.getAuditLog("a-1")).thenReturn(log);

        ResponseEntity<ApiSuccess<Page<AuditLogResponse>>> listResponse = reportsController.listAuditLogs(
                "ADMIN",
                AuditAction.BOOK,
                EntityType.BOOKING,
                "user-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                pageable);
        ResponseEntity<ApiSuccess<AuditLogResponse>> getResponse = reportsController.getAuditLog("ADMIN", "a-1");

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(1, listResponse.getBody().getData().getTotalElements());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("a-1", getResponse.getBody().getData().id());

        verify(reportsService).getAuditLog("a-1");
    }

    @Test
    void endpoints_withNonAdminRole_shouldThrowForbidden() {
        Pageable pageable = PageRequest.of(0, 1);
        assertThrows(ForbiddenException.class, () -> reportsController.driverReport("PASSENGER", null, null));
        assertThrows(ForbiddenException.class, () -> reportsController.bookingsReport("DRIVER", null, null));
        assertThrows(ForbiddenException.class, () -> reportsController.revenueReport("guest", null, null));
        assertThrows(ForbiddenException.class, () -> reportsController.passengerReport("USER", null, null));
        assertThrows(
                ForbiddenException.class,
                () -> reportsController.listAuditLogs("DRIVER", null, null, null, null, null, pageable));
        assertThrows(ForbiddenException.class, () -> reportsController.getAuditLog("PASSENGER", "a-1"));
    }
}
