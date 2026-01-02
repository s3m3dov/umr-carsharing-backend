# 🚗 Car Sharing Backend – Technical Roadmap (BlaBlaCar-like)

> **Focus:** Backend Engineering · Microservices · Stability · Observability  
> **Target:** A production-ready, scalable API for intercity carpooling.

---

## 🧭 Phase 0: Current State (Completed ✅)

- **Evolution:** Transformed from a monolithic prototype into a distributed microservices system to handle scalability.
- **Architecture:** Spring Cloud based (Eureka, API Gateway, Feign).
- **Security:** JWT Authentication and centralized authorization.
- **Data:** MongoDB Atlas with geospatial capabilities.
- **Logic:** Basic Trip offering, search (OSRM), and Join requests.

### Core Microservices Implemented

- **Auth Service** – Signup, login, JWT validation.
- **User Service** – Profile and user data management.
- **Vehicle Service** – Vehicle registration and listing.
- **Trip Service** – Trip lifecycle and OSRM-based routing.
- **Ride Service** – Booking logic and passenger coordination.
- **Notification Service** – Firebase-integrated realtime updates.

---

## 🛠️ Phase 1: Core Logic, Pricing & API Polish (TOP PRIORITY)

*Easy to implement features that provide immediate value and stability.*

### 1. Robust State Machines

- **Goal:** Prevent inconsistent states (e.g., joining a cancelled trip).
- **Trip States:** `CREATED` → `CONFIRMED` → `IN_PROGRESS` → `COMPLETED` → `CANCELLED`
- **Booking States:** `REQUESTED` → `ACCEPTED` → `REJECTED` → `CANCELLED`
- **Logic:** Guarded transitions and atomic seat count updates using MongoDB transactions.

### 2. Fare & Cost Calculation

- **Goal:** Keep pricing simple and driver-controlled in the initial version.
- **Features:**
  - **Driver-defined price:** Driver sets the total price per seat when creating a trip.
  - **Single currency:** All prices are handled and displayed in TZS.
  - **No automated pricing (for now):** Distance-based pricing recommendation, detour costs, and price suggestions are out of scope for MVP and planned for later phases.

### 3. API Quality & Developer Experience

- **Pagination & Sorting:** Standardize across all GET endpoints (Trips, Bookings, Users).
- **Filtering:** Search by price range, car type, and seat availability.
- **Error Handling:**
  - Implementation of custom `BusinessException` and `GlobalExceptionHandler`.
  - **Global Exception Mapping:** Automatically map `DuplicateKeyException` and `ConstraintViolationException` to readable business error codes.
  - **Standardized Response:** Every error returns `{ "errorCode": "...", "message": "...", "timestamp": "..." }`.
  - Structured error responses with unique codes (e.g., `TRIP_FULL`, `UNAUTHORIZED_ACCESS`).
- **API Versioning & Headers:**
  - Implement URI versioning (e.g., `/api/v1/...`).
  - Standardize Cache-Control headers for static data (like vehicle types or common locations).
- **Swagger/OpenAPI Enrichment:**
  - Add descriptive `@Operation` notes to all controllers.
  - Define explicit `@Schema` for all DTOs to improve auto-generated client libraries.
- **Input Validation & Sanitization:**
  - Strict JSR-303 (Hibernate Validator) constraints on all Request DTOs.
  - XSS sanitization for user-generated text (reviews, bios).
  - Proper data type enforcement (e.g., ISO-8601 for dates).

### 4. Structured Logging & Context

- **Correlation IDs:** API Gateway generates a `X-Correlation-ID` and propagates it through all service calls.
- **JSON Formatting:** Transition logs to JSON format (LogstashEncoder) for easy ingestion by ELK/Loki.
- **Request Tracing:** Add `MDC` (Mapped Diagnostic Context) to logs for user and request tracking.
- **Audit Logs:** Log critical actions (Trip creation, Booking acceptance) with UserID and Timestamp.
- **Event Persistence:** Save critical domain events (`TripPublished`, `BookingAccepted`, `TripCancelled`) to the database immediately to establish an audit trail for Phase 3 decoupling.

### 5. Backend Infrastructure Polish

- **OSRM Result Caching:** Cache external routing results (GeoJSON & distances) for popular routes to reduce API latency and cost.
- **Internal Security:** Implement "Secret Header" or mutual TLS between internal services to prevent direct unauthorized access bypassing the Gateway.
- **Graceful Shutdown & Environment management:**
  - Enable Spring Boot graceful shutdown to handle in-flight requests.
  - Standardize `application-{profile}.yml` (local, dev, prod) with secured env vars.
- **Admin & Support Utilities:**
  - Read-only endpoints for support staff to view user trip history.
  - Support for "Soft Deletes" (isDeleted flag) for Users and Vehicles to preserve data history.
  - "Maintenance Mode" flag at the API Gateway level to gracefully block traffic during updates.
- **Developer Productivity & Seeding:**
  - Automated database seeding (using `CommandLineRunner`) for local development.
  - Standardized Docker multi-stage builds for optimized image sizes.
  - Integrated `spring-boot-devtools` for faster local feedback loops.

---

## ⭐ Phase 2: Trust & Social Proof

*Relatively straightforward backend logic to build community trust.*

### 5. Review & Rating Service

- **New Service:** `review-service` for handling 1-5 star ratings and text reviews.
- **Validation:** Only allow reviews for `COMPLETED` trips where the user was a participant.
- **Aggregation:** Precompute and cache average ratings for User profiles.

### 6. User Statistics

- **Trip History:** Count of successful trips as driver vs. passenger.
- **Member Stats:** "Member since" tracking and "Response rate" metrics.

---

## 🔔 Phase 3: System Decoupling (Event-Driven Microservices)

*Enhancing reliability and side-effect management.*

### 7. Domain Events (Event-Driven Microservices)

- **Goal:** Move from synchronous calls to async event processing for side effects.
- **Implementation:** Transition from stored events (Phase 1) to active async processing using Spring Application Events (initial) or RabbitMQ for decoupling Notification and Audit services.

---

## 📊 Phase 4: Observability & Resilience

*Preparing the system for production loads and debugging.*

### 8. Monitoring & Health

- **Metrics:** Actuator integration for Prometheus.
- **Health Checks:**
  - Deep health checks for MongoDB, OSRM, and Firebase connectivity.
  - **Custom Health Indicators:** Build indicators for Google Maps API quota usage and OSRM response times.
- **Business Metric Indicators:** Custom Actuator endpoints to expose anonymous stats like "Total active trips" or "Successful matches today" for internal monitoring.
- **Distributed Tracing:** Micrometer Tracing to follow requests across microservices.

### 9. Resilience Patterns

- **Retries:** Configurable retries for OSRM route fetching and notification delivery.
- **Timeouts:** Strict timeouts on all inter-service (Feign) and external calls.
- **Circuit Breakers:** Prevent cascading failures when one service is down.

---

## 🚀 Phase 5: Performance & Scalability

*Original Phase 6.3 - Ensuring the system handles growth.*

### 10. Caching & Optimization

- **Redis Integration:** Cache frequent search results (e.g., popular routes).
- **DB Optimization:**
  - Review and tune MongoDB indexes for geospatial and compound queries.
  - **Query Profiling:** Identify and optimize "slow queries" using Mongo's explain plan.
- **Read Replicas:** Architecture ready for read/write splitting if load increases.

---

## 🛑 Phase 6: Complex Integrations

*To be implemented in a later stage (requires external partnerships/compliance).*

- **Payment Service Integration:** Stripe, M-Pesa, or other payment provider integrations.
- **Identity Verification:** KYC, Government ID verification, and Phone OTP flows.
- **Insurance:** Trip-specific passenger insurance integration.

---

## � Future Possibilities

*Nice-to-have features for future iterations.*

- **Advanced Route Matching:** Finding rides along a route with flexible detour tolerance.
- **Recurring Trips:** Support for daily/weekly commuter schedules.
- **Group Bookings:** Ability to book multiple seats at once with sub-passenger details.
- **AI-Driven Pricing:** Dynamic price suggestions based on demand and seasonality.

---

## 🏁 Final Technical Outcome

A **production-grade carpooling backend** characterized by:

- **Strong Consistency:** No overbooking or invalid state transitions.
- **High Observability:** Clear visibility into system health and logs.
- **Extreme Decoupling:** Services communicate via clear interfaces and events.
- **Geospatial Excellence:** Efficient matching of users on the move.
