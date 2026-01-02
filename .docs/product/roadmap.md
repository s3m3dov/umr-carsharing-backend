# 🚗 Car Sharing Backend – Full Roadmap (BlaBlaCar-like)

> **Tech stack:** Java 21 · Spring Boot · Spring Cloud · MongoDB Atlas · Docker · Firebase  
> **Architecture:** Microservices · API Gateway · Eureka · JWT Auth  
> **Status:** Advanced prototype → Production-ready platform

---

## 🧭 Phase 0 – Current State (Completed ✅)

### Architecture
- Microservices-based Spring Boot system
- API Gateway + Eureka Discovery
- JWT authentication & authorization
- MongoDB Atlas (geospatial + transactions)
- Docker Compose local orchestration

### Implemented Services
- **Auth Service** – Signup, login, JWT validation
- **User Service** – Profiles, user data
- **Vehicle Service** – Vehicle registration
- **Trip Service** – Trip creation, search, OSRM routing
- **Ride / Booking Logic** – Join trips, upcoming rides
- **Notification Service** – Firebase realtime notifications

---

## 🧩 Phase 1 – Core Domain Stability (TOP PRIORITY)

### 1. Trip & Booking Lifecycle
**Goal:** Prevent inconsistent states, race conditions, and invalid flows

#### Trip states
```

CREATED → CONFIRMED → IN_PROGRESS → COMPLETED → CANCELLED

```

#### Booking states
```

REQUESTED → ACCEPTED → REJECTED → CANCELLED

```

#### Required Features
- Driver accepts / rejects passenger requests
- Seat locking (atomic updates)
- Cancellation rules (driver vs passenger)
- No overbooking under concurrency

📌 **Deliverables**
- Explicit state machine enums
- Guarded state transitions
- Optimistic locking / Mongo transactions
- Clear business error codes

---

### 2. Authorization & Ownership Rules
**Goal:** Enforce business logic securely

- Driver can manage **only own trips**
- Passenger can manage **only own bookings**
- Role-based access (`DRIVER`, `PASSENGER`, `ADMIN`)
- Method-level security (`@PreAuthorize`)

📌 **Deliverables**
- Centralized authorization rules
- Gateway-level JWT propagation
- Proper 401 vs 403 handling

---

## ⭐ Phase 2 – Reviews & Ratings (MUST-HAVE)

### 3. Review & Rating System
**Goal:** Trust & reputation (core BlaBlaCar feature)

#### New Service
- **review-service**

#### Rules
- Trip must be `COMPLETED`
- Only real trip participants can review
- One review per trip per user
- Passenger ↔ Driver reviews separated

#### Data Model
- `Review`
- `UserRating` (denormalized, precomputed)

#### Features
- Create review
- Get user rating summary
- Paginated reviews
- Atomic rating updates

📌 **Deliverables**
- Mongo indexes & uniqueness constraints
- Review validation via Trip Service
- Aggregated rating cache

---

## 🔔 Phase 3 – Event-Driven Architecture (HIGH PRIORITY)

### 4. Domain Events
**Goal:** Loose coupling, scalability, clean side effects

#### Core Events
- BookingRequested
- BookingAccepted
- TripCancelled
- ReviewCreated

#### Consumers
- Notification Service
- Audit logging
- Future analytics & recommendations

📌 **Deliverables**
- Spring application events (initial)
- Async processing
- Event contracts
- Easy migration path to Kafka / RabbitMQ

---

## 📊 Phase 4 – Observability & Reliability (HIGH PRIORITY)

### 5. Monitoring & Tracing
**Goal:** Production readiness & debuggability

- Centralized logging (ELK / Loki)
- Metrics via Micrometer
- Distributed tracing across services

### 6. Resilience
- Circuit breakers (Resilience4j)
- Timeouts & retries for external APIs (OSRM, Firebase)
- Graceful degradation strategies

📌 **Deliverables**
- Dashboards for latency & error rates
- Dependency health visibility
- Fail-fast configurations

---

## 🧠 Phase 5 – Smart Matching & Ranking

### 7. Advanced Ride Matching
**Goal:** Competitive advantage

- Route overlap matching (including stops along the way)
- Detour tolerance
- Time flexibility windows
- Ranking by:
  - Rating
  - Price
  - Detour length

📌 **Deliverables**
- Enhanced geospatial queries
- Scoring algorithm
- Sorted, ranked search results

---

## 💳 Phase 6 – Payments (DEFERRED)

### 8. Payment Service
**Goal:** Monetization after core stability

- Fare calculation (distance × rate)
- Payment intent creation
- Capture on trip completion
- Refund on cancellation

📌 **Deliverables**
- `payment-service`
- Stripe integration
- Payment lifecycle states
- Secure webhooks

---

## 🔐 Phase 7 – Security Hardening

### 9. Security Enhancements
- Refresh tokens
- Token revocation
- Rate limiting (API Gateway)
- Input validation (GeoJSON, dates)

---

## 🧼 Phase 8 – API & DX Polish

### 10. API Quality
- Pagination everywhere
- Sorting & filtering
- API versioning (`/api/v1`)
- OpenAPI examples

---

## 🏁 Final Outcome

A **production-grade, interview-ready, scalable backend** with:
- Strong domain modeling
- Event-driven architecture
- Trust & reputation system
- Observability-first design
