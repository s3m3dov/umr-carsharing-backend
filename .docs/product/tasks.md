# 📋 Phase 1: Core Logic & Quality Polish – Task List

This file tracks the specific development tasks for Phase 1.

---

## 🏗️ 1. Robust State Machines

*Ensuring system consistency and preventing invalid transitions.*

- [ ] **Trip State Implementation**
  - [ ] Update `Trip` model to use `TripStatus` enum (`CREATED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
  - [ ] Implement `updateStatus` method in `TripService` with transition guards (e.g., cannot cancel a completed trip).
  - [ ] Add unit tests for valid/invalid state transitions.
- [ ] **Booking State Implementation**
  - [ ] Update `Booking` model to use `BookingStatus` enum (`REQUESTED`, `ACCEPTED`, `REJECTED`, `CANCELLED`).
  - [ ] Implement `processBookingRequest` logic:
    - [ ] Use MongoDB transactions (`@Transactional`) for atomicity.
    - [ ] Atomic decrement of `offeredSeat` count when booking is accepted.
    - [ ] Atomic increment of `offeredSeat` count when booking is cancelled or rejected.
  - [ ] Add integration tests for concurrent booking requests.

## 💰 2. Fare & Cost Calculation

*Simple, transparent pricing for the MVP.*

- [ ] **Driver Pricing Control**
  - [ ] Update `TripCreateRequest` and `Trip` model to include `pricePerSeat` (mandatory field).
  - [ ] Enforce minimum price validation (e.g., > 0 TZS).
- [ ] **Search & Display**
  - [ ] Update search response DTO to include `pricePerSeat`.
  - [ ] Add total trip price calculation for multi-seat bookings.

## 🛠️ 3. API Quality & DX

*Standardizing the developer and consumer experience.*

- [ ] **Pagination & Sorting**
  - [ ] Create `PageRequestDTO` and `PagedResponseDTO` in the `common` module.
  - [ ] Implement pagination in `TripRepository` and `BookingRepository`.
  - [ ] Update controllers to support `page`, `size`, and `sort` query parameters.
- [ ] **Search Filtering**
  - [ ] Add `minPrice` and `maxPrice` filters to `TripSearchRequest`.
  - [ ] Add `carType` and `minAvailableSeats` filters.
  - [ ] Implement dynamic query predicate builders using `CriteriaAPI` or QueryDSL.
- [ ] **Input Validation**
  - [ ] Apply JSR-303 annotations (`@NotNull`, `@NotBlank`, `@Min`, `@Size`, `@Future`) to all incoming DTOs.
  - [ ] Ensure nested objects are validated using `@Valid`.
  - [ ] Verify `GlobalExceptionHandler` correctly catches and transforms validation errors into standardized responses.

## 📝 4. Enterprise Logging & Context

*Establishing observability and audit trails.*

- [ ] **Correlation IDs (Tracing)**
  - [ ] Configure `api-gateway` to generate and append `X-Correlation-ID` to request headers.
  - [ ] Implement a `FeignRequestInterceptor` to pass the correlation ID between internal services.
  - [ ] Add a SLF4J/Logback Filter to include the ID in the MDC (Mapped Diagnostic Context) for every log line.
- [ ] **Structured JSON Logging**
  - [ ] Add `net.logstash.logback:logstash-logback-encoder` to the root `pom.xml`.
  - [ ] Create a standard `logback-spring.xml` template for all services.
  - [ ] Configure JSON output for the `prod` profile.
- [ ] **Audit Trail**
  - [ ] Create a dedicated `audit-log` collection in MongoDB.
  - [ ] Implement a lightweight `AuditAspect` or `AuditService` to intercept critical domain actions.
  - [ ] Persist `userId`, `action`, `correlationId`, `timestamp`, and `payload` for every major change.
