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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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

    public DriverPerformanceReport getDriverPerformanceReport(Instant from, Instant to) {
        Criteria criteria = createDateCriteria("createdAt", from, to);

        // Get distinct drivers count efficiently
        Query distinctDriversQuery = new Query(criteria);
        long totalDrivers = mongoTemplate
                .findDistinct(distinctDriversQuery, "driverId", Trip.class, String.class)
                .size();

        // Get trip counts by status using aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), Aggregation.group("tripStatus").count().as("count"));

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, Trip.class, Map.class);
        Map<String, Long> counts = results.getMappedResults().stream()
                .collect(Collectors.toMap(m -> m.get("_id").toString(), m -> (Long) m.get("count")));

        return DriverPerformanceReport.builder()
                .totalDrivers(totalDrivers)
                .totalTripsCompleted(counts.getOrDefault(TripStatus.COMPLETED.name(), 0L))
                .totalTripsCancelled(counts.getOrDefault(TripStatus.CANCELLED.name(), 0L))
                .totalTripsInProgress(counts.getOrDefault(TripStatus.IN_PROGRESS.name(), 0L))
                .totalTripsAvailable(counts.getOrDefault(TripStatus.AVAILABLE.name(), 0L))
                .build();
    }

    public BookingSummaryReport getBookingSummaryReport(Instant from, Instant to) {
        Criteria criteria = createDateCriteria("createdAt", from, to);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), Aggregation.group("status").count().as("count"));

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, Booking.class, Map.class);
        Map<String, Long> counts = results.getMappedResults().stream()
                .collect(Collectors.toMap(m -> m.get("_id").toString(), m -> (Long) m.get("count")));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        return BookingSummaryReport.builder()
                .totalBookings(total)
                .confirmedBookings(counts.getOrDefault(BookingStatus.CONFIRMED.name(), 0L))
                .cancelledBookings(counts.getOrDefault(BookingStatus.CANCELLED.name(), 0L))
                .requestedBookings(counts.getOrDefault(BookingStatus.REQUESTED.name(), 0L))
                .completedBookings(counts.getOrDefault(BookingStatus.COMPLETED.name(), 0L))
                .rejectedBookings(counts.getOrDefault(BookingStatus.REJECTED.name(), 0L))
                .build();
    }

    public RevenueReport getRevenueReport(Instant from, Instant to) {
        Criteria criteria = createDateCriteria("createdAt", from, to);
        criteria.and("status").is(BookingStatus.COMPLETED);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group()
                        .sum("estimatedPrice")
                        .as("totalRevenue")
                        .avg("estimatedPrice")
                        .as("averageRevenue")
                        .count()
                        .as("count"));

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, Booking.class, Map.class);
        Map result = results.getUniqueMappedResult();

        if (result == null) {
            return RevenueReport.builder()
                    .totalCompletedBookings(0)
                    .totalRevenue(0.0)
                    .averageRevenuePerBooking(0.0)
                    .build();
        }

        return RevenueReport.builder()
                .totalCompletedBookings(((Long) result.get("count")).intValue())
                .totalRevenue((Double) result.get("totalRevenue"))
                .averageRevenuePerBooking((Double) result.get("averageRevenue"))
                .build();
    }

    public PassengerActivityReport getPassengerActivityReport(Instant from, Instant to) {
        Criteria criteria = createDateCriteria("createdAt", from, to);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), Aggregation.group("status").count().as("count"));

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, Booking.class, Map.class);
        Map<String, Long> counts = results.getMappedResults().stream()
                .collect(Collectors.toMap(m -> m.get("_id").toString(), m -> (Long) m.get("count")));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long active = counts.getOrDefault(BookingStatus.CONFIRMED.name(), 0L)
                + counts.getOrDefault(BookingStatus.IN_PROGRESS.name(), 0L);

        return PassengerActivityReport.builder()
                .totalBookings(total)
                .activeBookings(active)
                .completedBookings(counts.getOrDefault(BookingStatus.COMPLETED.name(), 0L))
                .cancelledBookings(counts.getOrDefault(BookingStatus.CANCELLED.name(), 0L))
                .build();
    }

    private Criteria createDateCriteria(String field, Instant from, Instant to) {
        Criteria criteria = new Criteria();
        if (from != null || to != null) {
            Criteria timeCriteria = Criteria.where(field);
            if (from != null) {
                timeCriteria = timeCriteria.gte(from);
            }
            if (to != null) {
                timeCriteria = timeCriteria.lte(to);
            }
            return timeCriteria;
        }
        return criteria;
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
