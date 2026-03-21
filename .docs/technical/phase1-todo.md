# Phase 1 TODO - Core Logic & Quality Polish

> **Current Status:** ~60% Complete
> **Last Updated:** 2026-03-21

This document tracks the remaining work for Phase 1 of the Car Sharing Backend roadmap.

---

## Overview

Phase 1 focuses on refining state consistency, enhancing API quality, and implementing enterprise logging standards. The core microservices are functional, but several quality improvements are pending.

---

## Status Summary

| Category | Status | Completion |
|----------|--------|------------|
| State Machines | ⚠️ Partial | Trip: ✅ | Booking: ❌ |
| Fare & Cost Calculation | ⚠️ Partial | Missing driver-defined per-seat price |
| API Quality | ⚠️ Partial | Validation ✅ | Pagination ❌ | Filtering ⚠️ |
| Enterprise Logging | ⚠️ Partial | Correlation IDs ✅ | JSON Logging ❌ | Audit ❌ |

---

## Pending Items

### 1. Booking State Machine ❌

**Current State:**
- `BookingStatus` enum exists (`REQUESTED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)
- No transition validation logic implemented
- Bookings can transition to any state without guards

**Required Implementation:**

Create `BookingService` with state machine similar to `TripService`:

```java
private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS;

static {
    VALID_TRANSITIONS.put(BookingStatus.REQUESTED, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED));
    VALID_TRANSITIONS.put(BookingStatus.CONFIRMED, EnumSet.of(BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED));
    VALID_TRANSITIONS.put(BookingStatus.IN_PROGRESS, EnumSet.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
    VALID_TRANSITIONS.put(BookingStatus.COMPLETED, EnumSet.noneOf(BookingStatus.class));
    VALID_TRANSITIONS.put(BookingStatus.CANCELLED, EnumSet.noneOf(BookingStatus.class));
}

@Transactional
public void updateBookingStatus(String bookingId, BookingStatus newStatus) {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

    validateBookingTransition(booking.getStatus(), newStatus);

    booking.setStatus(newStatus);
    bookingRepository.save(booking);
}
```

**Acceptance Criteria:**
- Bookings cannot transition from `CONFIRMED` to `REQUESTED`
- Bookings cannot transition from `COMPLETED` to any other state
- All transitions are atomic with MongoDB transactions

---

### 2. Fare & Cost Calculation ⚠️

**Current State:**
- `Trip` model has `pricePerKm` field
- Driver sets price per kilometer, not per seat
- No currency handling (assumed TZS)

**Required Implementation:**

Add `pricePerSeat` field to allow driver-defined pricing:

```java
@Document(collection = "trips")
public class Trip {
    // ... existing fields

    @PositiveOrZero
    private double pricePerSeat;  // Total price per passenger seat

    private String currency = "TZS";  // Standardize currency
}
```

Update `OfferRideRequest`:

```java
public record OfferRideRequest(
    String driverId,
    String vehicleNumber,
    Points sourceAddress,
    Points destinationAddress,
    ZonedDateTime tripStartDateTime,
    @Min(value = 1, message = "Total seats must be at least 1") int totalSeats,

    // NEW: Driver-defined price per seat
    @PositiveOrZero(message = "Price must be non-negative") double pricePerSeat
) {
}
```

Update `Booking` calculation:

```java
// Calculate booking price based on requested seats
double bookingPrice = trip.getPricePerSeat() * booking.getRequestedSeats();
```

**Acceptance Criteria:**
- Driver sets total price per seat when offering a ride
- Booking price is calculated as `pricePerSeat × requestedSeats`
- Currency is consistently handled as TZS

---

### 3. Pagination ❌

**Current State:**
- No pagination support in controllers
- `TripController.searchMatchingRoute()` has TODO comment (line 84)
- All endpoints return full `List<Trip>` without limits

**Required Implementation:**

Add Spring Data `Pageable` support to search endpoints:

```java
@GetMapping("/search/matching-route")
public ResponseEntity<ApiResponse<Page<Trip>>> searchMatchingRoute(
    @RequestParam double sourceLatitude,
    @RequestParam double sourceLongitude,
    @RequestParam(defaultValue = "5.0") double sourceRadiusKm,
    @RequestParam double destinationLatitude,
    @RequestParam double destinationLongitude,
    @RequestParam(defaultValue = "5.0") double destinationRadiusKm,
    @RequestParam(required = false) String earliestDepartureTime,
    @RequestParam(required = false) String latestDepartureTime,
    @RequestParam Integer requestedSeats,
    @RequestParam String effectiveUserId,
    Pageable pageable) {  // NEW: Add Pageable

    Page<Trip> trips = geoService.findTripsMatchingRoute(
        sourceLatitude, sourceLongitude, sourceRadiusKm,
        destinationLatitude, destinationLongitude, destinationRadiusKm,
        earliestDepartureTime, latestDepartureTime,
        requestedSeats, effectiveUserId, pageable);  // Pass Pageable

    return ResponseEntity.ok(ApiResponse.success(trips));
}
```

Update repository methods:

```java
public interface TripsRepository extends MongoRepository<Trip, String> {

    // NEW: Return Page instead of List
    Page<Trip> findBySourceLocationNearAndTripStatusAndBookedSeatsLessThan(
        Point source, Distance distance, TripStatus status, int seats, Pageable pageable);
}
```

**Acceptance Criteria:**
- All search endpoints support `page`, `size`, and `sort` parameters
- Default page size: 20
- Maximum page size: 100
- Results include total count and pagination metadata

---

### 4. Advanced Filtering ⚠️

**Current State:**
- Basic filtering: radius, time range, seat availability
- Missing: price range, car type, vehicle preferences

**Required Implementation:**

Add filters to `TripController.searchMatchingRoute()`:

```java
@GetMapping("/search/matching-route")
public ResponseEntity<ApiResponse<Page<Trip>>> searchMatchingRoute(
    // ... existing params

    // NEW: Price range filters
    @RequestParam(required = false) Double minPrice,
    @RequestParam(required = false) Double maxPrice,

    // NEW: Vehicle type filter
    @RequestParam(required = false) String vehicleType,

    Pageable pageable) {

    // Filter by price range
    if (minPrice != null || maxPrice != null) {
        // Apply price constraints
    }

    // Filter by vehicle type
    if (vehicleType != null) {
        // Join with Vehicle Service to filter by type
    }

    // ...
}
```

**Acceptance Criteria:**
- Filter trips by minimum and maximum price per seat
- Filter trips by vehicle type (e.g., "SEDAN", "SUV", "HATCHBACK")
- Filters can be combined (AND logic)

---

### 5. JSON Logging ❌

**Current State:**
- Default Spring Boot text logging
- No structured logging configured
- Difficult to parse in ELK/Loki

**Required Implementation:**

Add `logstash-logback-encoder` dependency to `pom.xml`:

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

Create `src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app":"${spring.application.name}"}</customFields>
            <includeMdc>true</includeMdc>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE" />
    </root>
</configuration>
```

**Acceptance Criteria:**
- All logs output in JSON format
- `requestId` from MDC is included in each log entry
- Logs include application name, timestamp, level, logger, message

---

### 6. Audit Trail ❌

**Current State:**
- No audit event persistence
- Critical actions (booking, cancellation) not logged to database
- Only in-memory logs available

**Required Implementation:**

Create `AuditEvent` model:

```java
@Document(collection = "audit_events")
public class AuditEvent {
    @Id
    private String eventId;

    private String userId;
    private String action;  // e.g., "BOOKING_CREATED", "TRIP_CANCELLED"
    private String entityType;  // e.g., "Booking", "Trip"
    private String entityId;

    private String requestId;  // Correlation ID
    private Instant timestamp;

    private Map<String, Object> payload;  // Additional context
}
```

Create `AuditService`:

```java
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditRepository;
    private final RequestIdFilter requestIdFilter;

    public void logEvent(String userId, String action, String entityType, String entityId, Map<String, Object> payload) {
        AuditEvent event = AuditEvent.builder()
            .userId(userId)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .requestId(MDC.get(RequestIdFilter.MDC_KEY))
            .timestamp(Instant.now())
            .payload(payload)
            .build();

        auditRepository.save(event);
    }
}
```

Add audit logging to critical services:

```java
@Service
public class JoinTripService {

    private final AuditService auditService;

    public String joinTrip(JoinTripRequest request) {
        // ... existing logic

        // NEW: Audit log
        auditService.logEvent(
            request.passengerId(),
            "BOOKING_CREATED",
            "Booking",
            bookingId,
            Map.of(
                "tripId", request.tripId(),
                "requestedSeats", request.requestedSeats()
            )
        );

        return bookingId;
    }
}
```

**Acceptance Criteria:**
- All critical actions are logged to `audit_events` collection
- Each event includes userId, action, entity info, and correlation ID
- Audit log persists across service restarts

---

## Priority Order

1. **High Priority**: Booking State Machine, Audit Trail
2. **Medium Priority**: Pagination, Fare & Cost Calculation
3. **Low Priority**: JSON Logging, Advanced Filtering

---

## Related Files

- [Roadmap](../product/roadmap.md) - Full project roadmap
- [Architecture](./architecture.md) - System architecture overview
- [API Guide](./api-guide.md) - API documentation

---

## Next Phase

Once Phase 1 is complete, proceed to **Phase 2: Trust & Social Proof**:
- Review & Rating Service
- User Statistics