# TODO - Phase 1: Core Logic & Quality Polish

> **Current Status:** ~75% Complete
> **Last Updated:** 2026-03-25

This document tracks all work for Phase 1 - both completed and pending.

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
- `TripStatus`: `CREATED` → `AVAILABLE` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
- Transition validation in `TripService`
- Trips currently created directly as `AVAILABLE` (CREATED state exists but unused)

### 2. Booking State Machine ✅
- `BookingStatus`: `REQUESTED` → `REJECTED` | `CONFIRMED` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
- Added `REJECTED` state to match glossary
- Bookings start as `REQUESTED` (driver must accept/reject)
- Seat count decremented on rejection

### 3. Fare & Cost Calculation ✅
- Renamed `pricePerKm` → `pricePerSeat` throughout:
  - `Trip.java` model
  - `Booking.java` model
  - `TripResponse`, `DriverTripResponse`, `PassengerRideResponse`
  - `ActiveRidesService`, `TripSearchService`

### 4. API Restructuring ✅
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

### 5. Model Updates ✅
- Renamed `joinedRidersId` → `joinedPassengersId` in Trip model
- Renamed `pricePerKm` → `pricePerSeat` throughout

---

## Pending Items ❌

### Phase 1: API Quality & DX

#### Pagination & Sorting ❌

- [ ] Create `PageRequestDTO` and `PagedResponseDTO` in the `common` module.
- [ ] Implement pagination in `TripRepository` and `BookingRepository`.
- [ ] Update controllers to support `page`, `size`, and `sort` query parameters.

**Acceptance Criteria:**
- All search endpoints support `page`, `size`, and `sort` parameters
- Default page size: 20, Maximum: 100

---

#### Search Filtering ❌

- [ ] Add `minPrice` and `maxPrice` filters to `TripSearchRequest`.
- [ ] Add `carType` and `minAvailableSeats` filters.
- [ ] Implement dynamic query predicate builders.

**Implementation Status: Done ✅**

- Added `carType` field to Trip model
- Updated `/search/matching-route` endpoint with new filter params: `minPrice`, `maxPrice`, `carType`, `minAvailableSeats`
- Implemented price range filtering in MongoDB query
- Implemented minAvailableSeats filtering in-memory

---

#### Input Validation ❌

- [ ] Apply JSR-303 annotations (`@NotNull`, `@NotBlank`, `@Min`, `@Size`, `@Future`) to all incoming DTOs.
- [ ] Ensure nested objects are validated using `@Valid`.
- [ ] Verify `GlobalExceptionHandler` correctly catches validation errors.

---

### Phase 1: Enterprise Logging & Observability

#### Structured JSON Logging ❌

- [ ] Add `net.logstash.logback:logstash-logback-encoder` dependency to `pom.xml`.
- [ ] Create `logback-spring.xml` template in each service.
- [ ] Configure JSON output for `prod` profile.

---

#### Grafana & Loki Integration ❌

- [ ] Set up Loki as log aggregation backend
- [ ] Configure Grafana datasources for Loki
- [ ] Create dashboards for service health and metrics
- [ ] Add Prometheus metrics collection via Micrometer

---

#### Audit Trail ❌

- [ ] Create `audit_events` collection in MongoDB.
- [ ] Implement `AuditService` for critical domain actions.
- [ ] Persist `userId`, `action`, `correlationId`, `timestamp`, and `payload`.

---

### Phase 1: Trip Features

#### Trip Stops/Waypoints ❌

- [ ] Add `stops` field to Trip model (List<Points>)
- [ ] Update OfferRideRequest to accept stops
- [ ] Update OSRM routing to include waypoints
- [ ] Search by stops (not just endpoints)

---

## Priority Order

1. **High**: Pagination, Audit Trail
2. **Medium**: JSON Logging, Grafana/Loki, Search Filtering
3. **Low**: Input Validation, Trip Stops/Waypoints

---

## Related Files

- [roadmap.md](roadmap.md) - Full project roadmap
- [glossary.md](../domain/glossary.md) - Domain terminology
- [tasks.md](tasks.md) - Task list format

---

## Next Phase

Once Phase 1 is complete, proceed to **Phase 2: Trust & Social Proof**:
- Review & Rating Service
- User Statistics