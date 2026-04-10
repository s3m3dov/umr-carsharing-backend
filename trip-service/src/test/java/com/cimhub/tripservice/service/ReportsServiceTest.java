package com.cimhub.tripservice.service;

import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.model.AuditLog;
import com.cimhub.tripservice.dto.response.AuditLogResponse;
import com.cimhub.tripservice.dto.response.BookingSummaryReport;
import com.cimhub.tripservice.dto.response.DriverPerformanceReport;
import com.cimhub.tripservice.dto.response.PassengerActivityReport;
import com.cimhub.tripservice.dto.response.RevenueReport;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.report.ReportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    void getDriverPerformanceReport_shouldAggregateCounts() {
        Trip t1 = new Trip();
        t1.setDriverId("d1");
        Trip t2 = new Trip();
        t2.setDriverId("d2");
        Trip t3 = new Trip();
        t3.setDriverId("d1");

        when(tripsRepository.countByTripStatus(TripStatus.COMPLETED)).thenReturn(10L);
        when(tripsRepository.countByTripStatus(TripStatus.CANCELLED)).thenReturn(2L);
        when(tripsRepository.countByTripStatus(TripStatus.IN_PROGRESS)).thenReturn(1L);
        when(tripsRepository.countByTripStatus(TripStatus.AVAILABLE)).thenReturn(3L);
        when(tripsRepository.findDistinctDriverIds()).thenReturn(List.of(t1, t2, t3));

        DriverPerformanceReport report = reportsService.getDriverPerformanceReport();

        assertEquals(2L, report.totalDrivers());
        assertEquals(10L, report.totalTripsCompleted());
        assertEquals(2L, report.totalTripsCancelled());
        assertEquals(1L, report.totalTripsInProgress());
        assertEquals(3L, report.totalTripsAvailable());
    }

    @Test
    void getBookingSummaryReport_shouldAggregateStatusCounts() {
        when(bookingRepository.count()).thenReturn(20L);
        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(7L);
        when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(5L);
        when(bookingRepository.countByStatus(BookingStatus.REQUESTED)).thenReturn(4L);
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(3L);
        when(bookingRepository.countByStatus(BookingStatus.REJECTED)).thenReturn(1L);

        BookingSummaryReport report = reportsService.getBookingSummaryReport();

        assertEquals(20L, report.totalBookings());
        assertEquals(7L, report.confirmedBookings());
        assertEquals(5L, report.cancelledBookings());
        assertEquals(4L, report.requestedBookings());
        assertEquals(3L, report.completedBookings());
        assertEquals(1L, report.rejectedBookings());
    }

    @Test
    void getRevenueReport_shouldComputeTotalAndAverage() {
        Booking b1 = new Booking();
        b1.setEstimatedPrice(10.0);
        Booking b2 = new Booking();
        b2.setEstimatedPrice(20.0);

        when(bookingRepository.findByStatus(BookingStatus.COMPLETED)).thenReturn(List.of(b1, b2));

        RevenueReport report = reportsService.getRevenueReport();

        assertEquals(2L, report.totalCompletedBookings());
        assertEquals(30.0, report.totalRevenue());
        assertEquals(15.0, report.averageRevenuePerBooking());
    }

    @Test
    void getRevenueReport_whenNoBookings_shouldReturnZeroAverage() {
        when(bookingRepository.findByStatus(BookingStatus.COMPLETED)).thenReturn(List.of());

        RevenueReport report = reportsService.getRevenueReport();

        assertEquals(0L, report.totalCompletedBookings());
        assertEquals(0.0, report.totalRevenue());
        assertEquals(0.0, report.averageRevenuePerBooking());
    }

    @Test
    void getPassengerActivityReport_shouldComputeActiveCompletedCancelled() {
        when(bookingRepository.count()).thenReturn(15L);
        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(4L);
        when(bookingRepository.countByStatus(BookingStatus.IN_PROGRESS)).thenReturn(3L);
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(5L);
        when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(2L);

        PassengerActivityReport report = reportsService.getPassengerActivityReport();

        assertEquals(15L, report.totalBookings());
        assertEquals(7L, report.activeBookings());
        assertEquals(5L, report.completedBookings());
        assertEquals(2L, report.cancelledBookings());
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

        when(mongoTemplate.count(any(Query.class), org.mockito.ArgumentMatchers.eq(AuditLog.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), org.mockito.ArgumentMatchers.eq(AuditLog.class))).thenReturn(List.of(log));

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
