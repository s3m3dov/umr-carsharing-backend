# 🚗 Car Sharing Backend – Technical Roadmap (BlaBlaCar-like)

> **Focus:** Backend Engineering · Microservices · Stability · Observability  
> **Target:** A production-ready, scalable API for intercity carpooling.

---

## 🧭 Phase 0: Current State (Completed ✅)

- **Evolution:** Transformed from a monolithic prototype into a distributed microservices system.
- **Architecture:** Spring Cloud based (Kubernetes Discovery, API Gateway, Feign).
- **Security:** JWT Authentication and centralized authorization via Auth Service.
- **Data:** MongoDB Atlas with geospatial capabilities (2D Indexing).
- **Logic:** Trip offering, search (OSRM), and Booking management.
- **API Standards:** Implementation of `ApiResponse`, custom `BusinessException`, and global error handling.
- **Infrastructure:** Unified Docker builds with caching, supervisor-based orchestration, and standardized `run-compose.sh`.

### Core Microservices Implemented

- **Auth Service** – Signup, login, JWT validation.
- **User Service** – Profile and user data management.
- **Vehicle Service** – Vehicle registration and listing.
- **Trip Service** – Core domain logic:
  - Trip lifecycle (creation, search, OSRM routing).
  - Booking management (passengers, join/cancel requests).
- **Notification Service** – Firebase-integrated realtime updates.

---

## 🛠️ Phase 1: Core Logic & Quality Polish (IN PROGRESS 🏗️)

*Refining state consistency and enhancing developer experience.*

### 1. Robust State Machines (Completed ✅)

- **Goal:** Prevent inconsistent states (e.g., joining a cancelled trip).
- **Trip States:** `CREATED` → `AVAILABLE` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
- **Booking States:** `REQUESTED` → `REJECTED` | `CONFIRMED` → `IN_PROGRESS` → `COMPLETED` | `CANCELLED`
- **Logic:** Guarded transitions and atomic seat count updates using MongoDB transactions.
- **Changes:** Bookings now start as REQUESTED; driver must accept/reject. Trips created directly as AVAILABLE (CREATED state available but not used).

### 2. Trip Routing & Stops (Pending)

- **Goal:** Allow drivers to create flexible routes with intermediate stops.
- **Features:**
  - **Waypoints:** Driver can add pickup/dropoff stops along the route.
  - **Stop Management:** Add, update, or remove stops before trip starts.
  - **Route Calculation:** OSRM calculates optimal route with waypoints.
  - **Seat Constraints:** Stop-specific seat availability tracking.

### 3. Fare & Cost Calculation (Completed ✅)

- **Goal:** Keep pricing simple and driver-controlled in the initial version.
- **Changes:**
  - Renamed `pricePerKm` → `pricePerSeat` throughout codebase
  - Driver sets price per seat when creating a trip


### 4. API Quality & DX (In Progress ⚠️)

- [ ] **Pagination & Sorting:** Standardize across all GET endpoints (Trips, Bookings, Users). [Pending]
- [ ] **Filtering:** Search by price range, car type, and seat availability. [Pending]
- [x] **Error Handling:** Standardized `ApiResponse` and `ApiError` with business-specific codes.
- [x] **OpenAPI/Swagger:** Dynamic configuration with CORS support and Gateway aggregation.
- [x] **API Restructuring:** Created RideController for passengers, updated BookingController for drivers. [Completed]
- [ ] **Input Validation:** Strict JSR-303 constraints on all Request DTOs. [Pending]

### 5. Enterprise Logging & Context

- [x] **Correlation IDs:** API Gateway propagation through all services. [Completed]
- [ ] **JSON Logging:** Transition to LogstashEncoder for ELK/Loki readiness. [Pending]
- [x] **MDC Tracing:** Request and user context in logs. [Completed]
- [ ] **Audit Trail:** Event persistence for critical domain actions. [Pending]

---

## ⭐ Phase 2: Trust & Social Proof

*Relatively straightforward backend logic to build community trust.*

### 6. Review & Rating Service

- **New Service:** `review-service` for handling 1-5 star ratings and text reviews.
- **Validation:** Only allow reviews for `COMPLETED` trips where the user was a participant.
- **Aggregation:** Precompute and cache average ratings for User profiles.

### 7. User Statistics

- **Trip History:** Count of successful trips as driver vs. passenger.
- **Member Stats:** "Member since" tracking and "Response rate" metrics.

---

## 🔔 Phase 3: System Decoupling (Event-Driven Microservices)

*Enhancing reliability and side-effect management.*

### 8. Domain Events (Event-Driven Microservices)

- **Goal:** Move from synchronous calls to async event processing for side effects.
- **Implementation:** Transition from stored events (Phase 1) to active async processing using Spring Application Events (initial) or RabbitMQ for decoupling Notification and Audit services.

---

## 📊 Phase 4: Observability & Resilience

*Preparing the system for production loads and debugging.*

### 9. Monitoring & Health

- **Metrics:** Actuator integration for Prometheus.
- **Health Checks:**
  - Deep health checks for MongoDB, OSRM, and Firebase connectivity.
  - **Custom Health Indicators:** Build indicators for Google Maps API quota usage and OSRM response times.
- **Business Metric Indicators:** Custom Actuator endpoints to expose anonymous stats like "Total active trips" or "Successful matches today" for internal monitoring.
- **Distributed Tracing:** Micrometer Tracing to follow requests across microservices.

### 10. Resilience Patterns

- **Retries:** Configurable retries for OSRM route fetching and notification delivery.
- **Timeouts:** Strict timeouts on all inter-service (Feign) and external calls.
- **Circuit Breakers:** Prevent cascading failures when one service is down.

---

## 🚀 Phase 5: Performance & Scalability

*Original Phase 6.3 - Ensuring the system handles growth.*

### 11. Caching & Optimization

- **Redis Integration:** Cache frequent search results (e.g., popular routes).
- **DB Optimization:**
  - Review and tune MongoDB indexes for geospatial and compound queries.
  - **Query Profiling:** Identify and optimize "slow queries" using Mongo's explain plan.
- **Read Replicas:** Architecture ready for read/write splitting if load increases.

---

## 🛑 Phase 6: Post-MVP & Integrations

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
