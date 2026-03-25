# 📋 Phase 1: Core Logic & Quality Polish – Task List

This file tracks the specific development tasks for Phase 1.

---

## 🏗️ 1. Robust State Machines ✅ COMPLETED

*Ensuring system consistency and preventing invalid transitions.*

- [x] **Trip State Implementation**
  - [x] `TripStatus` enum: `OFFERED` → `AVAILABLE` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
  - [x] Implement `updateStatus` method in `TripService` with transition guards
- [x] **Booking State Implementation**
  - [x] `BookingStatus` enum: `REQUESTED` → `REJECTED` | `CONFIRMED` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
  - [x] Added `REJECTED` state to match glossary
  - [x] Bookings start as `REQUESTED` (driver must accept/reject)
  - [x] Atomic decrement of `bookedSeats` when booking is rejected
  - [x] Driver can accept or reject bookings via BookingController

## 💰 2. Fare & Cost Calculation ✅ COMPLETED

*Simple, transparent pricing for the MVP.*

- [x] **Driver Pricing Control**
  - [x] Renamed `pricePerKm` → `pricePerSeat` in Trip, Booking, and DTOs
  - [x] Driver sets price per seat when creating a trip
- [x] **Search & Display**
  - [x] Updated search response DTOs to include `pricePerSeat`

## 🛠️ 3. API Quality & DX ⚠️ IN PROGRESS

*Standardizing the developer and consumer experience.*

- [x] **API Restructuring** (Completed)
  - [x] Created `RideController` for passengers (`/api/rides`)
  - [x] Updated `BookingController` for drivers (`/api/bookings/accept`, `/api/bookings/reject`)
  - [x] Added separate Swagger tags for Trip Management vs Trip Search
  
- [ ] **Pagination & Sorting** (Pending)
  - [ ] Create `PageRequestDTO` and `PagedResponseDTO` in the `common` module.
  - [ ] Implement pagination in `TripRepository` and `BookingRepository`.
  - [ ] Update controllers to support `page`, `size`, and `sort` query parameters.
  
- [x] **Search Filtering** (Completed)
  - [x] Add `minPrice` and `maxPrice` filters to `TripSearchRequest`.
  - [x] Add `carType` and `minAvailableSeats` filters.
  - [x] Implement dynamic query predicate builders.
  
- [ ] **Input Validation** (Pending)
  - [ ] Apply JSR-303 annotations (`@NotNull`, `@NotBlank`, `@Min`, `@Size`, `@Future`) to all incoming DTOs.
  - [ ] Ensure nested objects are validated using `@Valid`.
  - [ ] Verify `GlobalExceptionHandler` correctly catches validation errors.

## 🛑 4. Trip Stops/Waypoints (Planned)

- [ ] Add `stops` field to Trip model (List<Points>)
- [ ] Update OfferRideRequest to accept stops
- [ ] Update OSRM routing to include waypoints
- [ ] Search by stops (not just endpoints)

## 📝 5. Enterprise Logging & Context ⚠️ PARTIAL

*Establishing observability and audit trails.*

- [x] **Correlation IDs (Tracing)** (Completed)
  - [x] API Gateway generates `X-Correlation-ID`
  - [x] RequestIdFilter propagates correlation ID through services
  
- [ ] **Structured JSON Logging** (Pending)
  - [ ] Add `net.logstash.logback:logstash-logback-encoder` dependency.
  - [ ] Create `logback-spring.xml` template.
  - [ ] Configure JSON output for `prod` profile.
  
- [ ] **Grafana & Loki Integration** (Pending)
  - [ ] Set up Loki as log aggregation backend
  - [ ] Configure Grafana datasources for Loki
  - [ ] Create dashboards for service health and metrics
  - [ ] Add Prometheus metrics collection via Micrometer
  
- [ ] **Audit Trail** (Pending)
  - [ ] Create `audit_events` collection in MongoDB.
  - [ ] Implement `AuditService` for critical domain actions.
  - [ ] Persist `userId`, `action`, `correlationId`, `timestamp`, and `payload`.

---

## Priority Order

1. **High**: Pagination, Audit Trail
2. **Medium**: JSON Logging, Search Filtering
3. **Low**: Trip Stops/Waypoints, Input Validation