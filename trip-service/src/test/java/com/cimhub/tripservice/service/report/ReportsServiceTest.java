package com.cimhub.tripservice.service.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.model.AuditLog;
import com.cimhub.tripservice.dto.response.AuditLogResponse;
import com.cimhub.tripservice.dto.response.BookingSummaryReport;
import com.cimhub.tripservice.dto.response.DriverPerformanceReport;
import com.cimhub.tripservice.dto.response.PassengerActivityReport;
import com.cimhub.tripservice.dto.response.RevenueReport;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class ReportsServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private ReportsService reportsService;

    @BeforeEach
    void setUp() {
        reportsService = new ReportsService(tripsRepository, bookingRepository, mongoTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getDriverPerformanceReport_shouldAggregateCounts() {
        when(mongoTemplate.findDistinct(any(Query.class), eq("driverId"), eq(Trip.class), eq(String.class)))
                .thenReturn(List.of("d1", "d2"));

        AggregationResults<Map> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults())
                .thenReturn(List.of(
                        Map.of("_id", TripStatus.COMPLETED.name(), "count", 10L),
                        Map.of("_id", TripStatus.CANCELLED.name(), "count", 2L),
                        Map.of("_id", TripStatus.IN_PROGRESS.name(), "count", 1L),
                        Map.of("_id", TripStatus.AVAILABLE.name(), "count", 3L)));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Trip.class), eq(Map.class)))
                .thenReturn(aggregationResults);

        DriverPerformanceReport report = reportsService.getDriverPerformanceReport(null, null);

        assertEquals(2L, report.totalDrivers());
        assertEquals(10L, report.totalTripsCompleted());
        assertEquals(2L, report.totalTripsCancelled());
        assertEquals(1L, report.totalTripsInProgress());
        assertEquals(3L, report.totalTripsAvailable());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getBookingSummaryReport_shouldAggregateStatusCounts() {
        AggregationResults<Map> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults())
                .thenReturn(List.of(
                        Map.of("_id", BookingStatus.CONFIRMED.name(), "count", 7L),
                        Map.of("_id", BookingStatus.CANCELLED.name(), "count", 5L),
                        Map.of("_id", BookingStatus.REQUESTED.name(), "count", 4L),
                        Map.of("_id", BookingStatus.COMPLETED.name(), "count", 3L),
                        Map.of("_id", BookingStatus.REJECTED.name(), "count", 1L)));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Booking.class), eq(Map.class)))
                .thenReturn(aggregationResults);

        BookingSummaryReport report = reportsService.getBookingSummaryReport(null, null);

        assertEquals(20L, report.totalBookings());
        assertEquals(7L, report.confirmedBookings());
        assertEquals(5L, report.cancelledBookings());
        assertEquals(4L, report.requestedBookings());
        assertEquals(3L, report.completedBookings());
        assertEquals(1L, report.rejectedBookings());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRevenueReport_shouldComputeTotalAndAverage() {
        AggregationResults<Map> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getUniqueMappedResult())
                .thenReturn(Map.of(
                        "totalRevenue", 30.0,
                        "averageRevenue", 15.0,
                        "count", 2L));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Booking.class), eq(Map.class)))
                .thenReturn(aggregationResults);

        RevenueReport report = reportsService.getRevenueReport(null, null);

        assertEquals(2, report.totalCompletedBookings());
        assertEquals(30.0, report.totalRevenue());
        assertEquals(15.0, report.averageRevenuePerBooking());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRevenueReport_whenNoBookings_shouldReturnZeroAverage() {
        AggregationResults<Map> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(null);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Booking.class), eq(Map.class)))
                .thenReturn(aggregationResults);

        RevenueReport report = reportsService.getRevenueReport(null, null);

        assertEquals(0, report.totalCompletedBookings());
        assertEquals(0.0, report.totalRevenue());
        assertEquals(0.0, report.averageRevenuePerBooking());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPassengerActivityReport_shouldComputeActiveCompletedCancelled() {
        AggregationResults<Map> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults())
                .thenReturn(List.of(
                        Map.of("_id", BookingStatus.CONFIRMED.name(), "count", 4L),
                        Map.of("_id", BookingStatus.IN_PROGRESS.name(), "count", 3L),
                        Map.of("_id", BookingStatus.COMPLETED.name(), "count", 5L),
                        Map.of("_id", BookingStatus.CANCELLED.name(), "count", 3L))); // Total 15
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Booking.class), eq(Map.class)))
                .thenReturn(aggregationResults);

        PassengerActivityReport report = reportsService.getPassengerActivityReport(null, null);

        assertEquals(15L, report.totalBookings());
        assertEquals(7L, report.activeBookings());
        assertEquals(5L, report.completedBookings());
        assertEquals(3L, report.cancelledBookings());
    }

    @Test
    void getAuditLogs_shouldReturnMappedPage() {
        AuditLog log = AuditLog.builder()
                .id("a1")
                .action(AuditAction.BOOK)
                .entityType(EntityType.BOOKING)
                .entityId("b1")
                .performedBy("user-1")
                .details("Booked")
                .serviceName("trip-service")
                .timestamp(Instant.parse("2026-01-01T10:00:00Z"))
                .build();

        when(mongoTemplate.count(any(Query.class), org.mockito.ArgumentMatchers.eq(AuditLog.class)))
                .thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), org.mockito.ArgumentMatchers.eq(AuditLog.class)))
                .thenReturn(List.of(log));

        Page<AuditLogResponse> page = reportsService.getAuditLogs(
                AuditAction.BOOK,
                EntityType.BOOKING,
                "user-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                Pageable.unpaged());

        assertEquals(1L, page.getTotalElements());
        assertEquals("a1", page.getContent().getFirst().id());
        assertEquals(AuditAction.BOOK, page.getContent().getFirst().action());
        assertEquals(EntityType.BOOKING, page.getContent().getFirst().entityType());
    }

    @Test
    void getAuditLog_whenFound_shouldReturnMappedResponse() {
        AuditLog log = AuditLog.builder()
                .id("a2")
                .action(AuditAction.UPDATE)
                .entityType(EntityType.TRIP)
                .entityId("trip-1")
                .performedBy("admin")
                .details("Updated")
                .serviceName("trip-service")
                .timestamp(Instant.parse("2026-02-01T10:00:00Z"))
                .build();

        when(mongoTemplate.findById("a2", AuditLog.class)).thenReturn(log);

        AuditLogResponse response = reportsService.getAuditLog("a2");

        assertEquals("a2", response.id());
        assertEquals(AuditAction.UPDATE, response.action());
        assertEquals(EntityType.TRIP, response.entityType());
        assertEquals("trip-1", response.entityId());
    }

    @Test
    void getAuditLog_whenMissing_shouldThrowNotFound() {
        when(mongoTemplate.findById("missing", AuditLog.class)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> reportsService.getAuditLog("missing"));
    }
}
