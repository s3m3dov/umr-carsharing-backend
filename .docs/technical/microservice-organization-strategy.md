# Microservice Organization & Evolution Strategy

This document outlines the strategic organization of microservices for the Car Sharing Platform, balancing current simplicity with long-term scalability inspired by BlaBlaCar’s engineering journey.

## Core Philosophy
We follow **Domain-Driven Design (DDD)** principles, organizing services around **Bounded Contexts** rather than technical layers. The goal is to minimize inter-service coupling and avoid a "distributed monolith."

---

## Phase 1: Pragmatic Consolidation (The "Middle Ground")
*Focus: Backend Simplicity WITHOUT Mobile API Changes.*

We want to reduce backend complexity by grouping related functions into **"Macro-services"**, but we **MUST NOT break the mobile team's current API contracts**. We achieve this by using the **API Gateway Façade Pattern**.

We will merge the code and deployments on the backend where it makes sense, but the `api-gateway` will continue to expose the exact same legacy path prefixes (`/user-service/**`, `/trip-service/**`, etc.) and route them to the appropriate services. The mobile app will not know the backend has changed.

### 1. Auth Service
*   **Status**: Keep separate.
*   **Gateway Routing (No Mobile Change)**:
    *   `/auth-service/**` ➔ Routes to `auth-service`
*   **Rationale**: Authentication and token generation (JWT) is highly sensitive, often has distinct scaling requirements, and serves as the security perimeter for the entire platform. Keeping it isolated ensures security boundaries are maintained.

### 2. User Service
*   **Merges Backend Code**: `user-service` + `vehicle-service`.
*   **Gateway Routing (No Mobile Change)**:
    *   `/user-service/**` ➔ Routes to `user-service`
    *   `/vehicle-service/**` ➔ Routes to `user-service`
*   **Rationale**: These domains are tightly coupled around the "Actor". A vehicle intrinsically belongs to a user (driver). Merging them eliminates network overhead when validating if a driver has a registered vehicle.

### 3. Trip Service
*   **Merges Backend Code**: `trip-service` + `ride-service`.
*   **Gateway Routing (No Mobile Change)**:
    *   `/trip-service/**` ➔ Routes to `trip-service`
    *   `/ride-service/**` ➔ Routes to `trip-service`
*   **Rationale**: A **Ride** (booking) is inherently a segment of a **Trip**. Splitting them early forces complex distributed transactions to manage seat availability. Managing the entire journey lifecycle in one place guarantees data consistency.

### 4. Review Service
*   **Status**: New Dedicated Service.
*   **Gateway Routing (No Mobile Change)**:
    *   `/review-service/**` ➔ Routes to `review-service`
*   **Rationale**: Ratings and reviews are critical for community trust but follow a different lifecycle and have high read volume. This service calculates user reputations, aggregates scores, and handles dispute resolution. Extracting this early creates a solid foundation for the trust system.

### 5. Notification Service
*   **Status**: Keep separate.
*   **Gateway Routing (No Mobile Change)**:
    *   `/notification-service/**` ➔ Routes to `notification-service`
*   **Rationale**: Cross-cutting and inherently asynchronous (Emails, SMS, Push). It should scale independently based on external provider throughput (e.g., Firebase).

---

## Phase 2: Strategic Scalability (Post-MVP Growth)
*Focus: Performance, Specialization, and Monetization.*

As the platform grows, we extract specific domains into dedicated services to handle complex business logic and high load.

### 1. Payment Service
*   **New Dedicated Service**: `payment-service`.
*   **Rationale**: Handles sensitive financial data, escrow, ride payments, and driver payouts. This domain requires high security (PCI compliance considerations), strict audit logs, and integrations with payment gateways (Stripe, Mobile Money, etc.).
*   **Pattern**: Implements strict Hexagonal Architecture to isolate core financial logic from external gateway adapters.

### 2. Trip Search Service
*   **Extracted from**: Trip Service.
*   **Rationale**: Search is read-heavy and requires specialized technology (e.g., Elasticsearch) for geospatial matching and filtering. 
*   **Tech Change**: Introduce an event-driven sync (Kafka) where `TripPublished` events are indexed into the Search Service.

---

## Phase 3: Domain Maturity (BlaBlaCar Scale)
*Focus: Total Autonomy and Fine-grained Control.*

At "BlaBlaCar scale", the architecture is refined into micro-segments.

### 1. Ridemap / GIS Service
*   Dedicated to calculating routes, geographic distances, detours, and matching pickups on a map (OpenStreetMap integration).

### 2. Fraud & Verification Service
*   ML-driven service to detect fake profiles, suspicious booking patterns, and automated scrapers.

### 3. Change Data Capture (CDC) Pipeline
*   Use of **Debezium + Kafka** to stream database changes to data warehouses (BigQuery) and other services, removing the need for direct HTTP polling.

---

## Service Mapping Summary

| Current Service / Concept | Phase 1 (Gateway Façade) | Phase 2+ (Evolution) |
| :--- | :--- | :--- |
| `auth-service` | **Auth Service** | Identity (Auth) |
| `user-service` | **User Service** | User Management |
| `vehicle-service` | **User Service** | Fleet / Logistics |
| `trip-service` | **Trip Service** | Trip Listing / Search |
| `ride-service` | **Trip Service** | Booking / Lifecycle |
| *Reviews/Ratings* | **Review Service** | Trust & Reputation |
| `notification-service` | **Notification Service** | Engagement / CRM |
| *Payments* | *(Not implemented)* | **Payment Service** |

---

## Consistency & Communication Rules

1.  **Prefer Asynchronous for Side-Effects**: If action A results in notification B, use an event/message broker.
2.  **Synchronous for Critical Intent**: Booking a seat (Trip Service) or verifying a token (Auth Service) should be synchronous API calls to ensure immediate validation.
3.  **No Shared Databases**: Even if two concepts are merged into a Macro-service (e.g., users and vehicles), they should ideally maintain logical schema separation to make future extraction easier.
4.  **Gateway-Driven**: All external traffic MUST go through the `api-gateway` for routing to preserve mobile API contracts.