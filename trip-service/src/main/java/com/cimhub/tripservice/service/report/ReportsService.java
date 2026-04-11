package com.cimhub.tripservice.service.report;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsService {

    private final TripsRepository tripsRepository;
    private final BookingRepository bookingRepository;
    private final MongoTemplate mongoTemplate;

    public DriverPerformanceReport getDriverPerformanceReport() {
        long completed = tripsRepository.countByTripStatus(TripStatus.COMPLETED);
        long cancelled = tripsRepository.countByTripStatus(TripStatus.CANCELLED);
        long inProgress = tripsRepository.countByTripStatus(TripStatus.IN_PROGRESS);
        long available = tripsRepository.countByTripStatus(TripStatus.AVAILABLE);

        List<Trip> tripsWithDrivers = tripsRepository.findDistinctDriverIds();
        long totalDrivers =
                tripsWithDrivers.stream().map(Trip::getDriverId).distinct().count();

        return DriverPerformanceReport.builder()
                .totalDrivers(totalDrivers)
                .totalTripsCompleted(completed)
                .totalTripsCancelled(cancelled)
                .totalTripsInProgress(inProgress)
                .totalTripsAvailable(available)
                .build();
    }

    public BookingSummaryReport getBookingSummaryReport() {
        long total = bookingRepository.count();
        long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        long requested = bookingRepository.countByStatus(BookingStatus.REQUESTED);
        long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long rejected = bookingRepository.countByStatus(BookingStatus.REJECTED);

        return BookingSummaryReport.builder()
                .totalBookings(total)
                .confirmedBookings(confirmed)
                .cancelledBookings(cancelled)
                .requestedBookings(requested)
                .completedBookings(completed)
                .rejectedBookings(rejected)
                .build();
    }

    public RevenueReport getRevenueReport() {
        List<Booking> completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED);
        double totalRevenue = completedBookings.stream()
                .mapToDouble(Booking::getEstimatedPrice)
                .sum();
        double avgRevenue = completedBookings.isEmpty() ? 0.0 : totalRevenue / completedBookings.size();

        return RevenueReport.builder()
                .totalCompletedBookings(completedBookings.size())
                .totalRevenue(totalRevenue)
                .averageRevenuePerBooking(avgRevenue)
                .build();
    }

    public PassengerActivityReport getPassengerActivityReport() {
        long total = bookingRepository.count();
        long active = bookingRepository.countByStatus(BookingStatus.CONFIRMED)
                + bookingRepository.countByStatus(BookingStatus.IN_PROGRESS);
        long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        return PassengerActivityReport.builder()
                .totalBookings(total)
                .activeBookings(active)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .build();
    }

    public Page<AuditLogResponse> getAuditLogs(
            AuditAction action,
            EntityType entityType,
            String performedBy,
            Instant from,
            Instant to,
            Pageable pageable) {
        Query query = new Query();
        List<Criteria> filters = new ArrayList<>();

        if (action != null) {
            filters.add(Criteria.where("action").is(action));
        }
        if (entityType != null) {
            filters.add(Criteria.where("entityType").is(entityType));
        }
        if (performedBy != null && !performedBy.isBlank()) {
            filters.add(Criteria.where("performedBy").is(performedBy));
        }

        if (from != null || to != null) {
            Criteria timeCriteria = Criteria.where("timestamp");
            if (from != null) {
                timeCriteria = timeCriteria.gte(from);
            }
            if (to != null) {
                timeCriteria = timeCriteria.lte(to);
            }
            filters.add(timeCriteria);
        }

        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, AuditLog.class);
        if (pageable.getSort().isUnsorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        }
        query.with(pageable);

        List<AuditLogResponse> logs = mongoTemplate.find(query, AuditLog.class).stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(logs, pageable, total);
    }

    public AuditLogResponse getAuditLog(String id) {
        AuditLog auditLog = Optional.ofNullable(mongoTemplate.findById(id, AuditLog.class))
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found: " + id));
        return toResponse(auditLog);
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .performedBy(auditLog.getPerformedBy())
                .details(auditLog.getDetails())
                .serviceName(auditLog.getServiceName())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
