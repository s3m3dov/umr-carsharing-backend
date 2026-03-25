# Phase 1 TODO - Core Logic & Quality Polish

> **Current Status:** ~75% Complete
> **Last Updated:** 2026-03-25

This document tracks the remaining work for Phase 1 of the Car Sharing Backend roadmap.

---

## Overview

Phase 1 focuses on refining state consistency, enhancing API quality, and implementing enterprise logging standards. The core microservices are functional, but several quality improvements are pending.

---

## Status Summary

| Category | Status | Completion |
|----------|--------|------------|
| State Machines | ✅ Complete | Trip: ✅ | Booking: ✅ |
| Fare & Cost Calculation | ✅ Complete | `pricePerKm` → `pricePerSeat` |
| API Quality | ⚠️ Partial | Validation ⚠️ | Pagination ❌ | Filtering ❌ |
| Enterprise Logging | ⚠️ Partial | Correlation IDs ✅ | JSON Logging ❌ | Audit ❌ |

---

## Completed Items ✅

### 1. Trip State Machine ✅

**Implemented:**
- `TripStatus`: `CREATED` → `AVAILABLE` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
- Transition validation in `TripService`
- Trips currently created directly as `AVAILABLE` (CREATED state exists but unused)

---

### 2. Booking State Machine ✅

**Implemented:**
- `BookingStatus`: `REQUESTED` → `REJECTED` | `CONFIRMED` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
- Added `REJECTED` state to match glossary
- Bookings start as `REQUESTED` (driver must accept/reject)
- Seat count decremented on rejection

---

### 3. Fare & Cost Calculation ✅

**Implemented:**
- Renamed `pricePerKm` → `pricePerSeat` throughout:
  - `Trip.java` model
  - `Booking.java` model
  - `TripResponse`, `DriverTripResponse`, `PassengerRideResponse`
  - `ActiveRidesService`, `TripSearchService`

---

### 4. API Restructuring ✅

**Implemented:**
- **New RideController** (passenger-facing):
  - `POST /api/rides/book` - Book a ride
  - `POST /api/rides/cancel` - Cancel ride
  - `GET /api/rides/active/passenger/{passengerId}` - Active rides
  - `GET /api/rides/history/passenger/{passengerId}` - Ride history
  
- **Updated BookingController** (driver-facing):
  - `POST /api/bookings/accept` - Accept booking request
  - `POST /api/bookings/reject` - Reject booking request

- **Separate Swagger Tags** in TripController:
  - `@Tag(name = "Trip Management")` - Driver operations
  - `@Tag(name = "Trip Search")` - Passenger search operations

---

### 5. Model Updates ✅

**Implemented:**
- Renamed `joinedRidersId` → `joinedPassengersId` in Trip model

---

## Pending Items ❌

### 1. Pagination ❌

**Current State:**
- No pagination support in controllers
- All endpoints return full `List<T>` without limits

**Required Implementation:**

Add Spring Data `Pageable` support to search endpoints:

```java
@GetMapping("/search/matching-route")
public ResponseEntity<ApiResponse<Page<Trip>>> searchMatchingRoute(
    // ... existing params
    Pageable pageable) {

    Page<Trip> trips = geoService.findTripsMatchingRoute(..., pageable);
    return ResponseEntity.ok(ApiResponse.success(trips));
}
```

**Acceptance Criteria:**
- All search endpoints support `page`, `size`, and `sort` parameters
- Default page size: 20, Maximum: 100

---

### 2. Advanced Filtering ❌

**Current State:**
- Basic filtering: radius, time range, seat availability
- Missing: price range, vehicle type

**Required Implementation:**

Add filters to search endpoints:
- `@RequestParam(required = false) Double minPrice`
- `@RequestParam(required = false) Double maxPrice`

---

### 3. JSON Logging ❌

**Current State:**
- Default Spring Boot text logging

**Required Implementation:**

Add `logstash-logback-encoder` and configure JSON logging.

---

### 4. Audit Trail ❌

**Current State:**
- No audit event persistence

**Required Implementation:**

Create `AuditEvent` model and `AuditService` for critical actions.

---

### 5. Trip Stops/Waypoints ❌ (Planned)

**Feature:** Drivers can add intermediate stops to their route

**Required Implementation:**
- Add `stops` field to Trip model (List<Points>)
- Update OfferRideRequest to accept stops
- Update OSRM routing to include waypoints

---

## Priority Order

1. **High Priority**: Pagination, Audit Trail
2. **Medium Priority**: JSON Logging, Advanced Filtering
3. **Low Priority**: Trip Stops/Waypoints

---

## Related Files

- [Roadmap](roadmap.md) - Full project roadmap
- [Glossary](../domain/glossary.md) - Domain terminology
- [Implementation Status](../technical/implementation-status.md) - Change log

---

## Next Phase

Once Phase 1 is complete, proceed to **Phase 2: Trust & Social Proof**:
- Review & Rating Service
- User Statistics