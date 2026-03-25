# Microservice Organization & Evolution Strategy

This document outlines the strategic organization of microservices for the Car Sharing Platform, balancing current simplicity with long-term scalability inspired by BlaBlaCar’s engineering journey.

## Core Philosophy
We follow **Domain-Driven Design (DDD)** principles, organizing services around **Bounded Contexts** rather than technical layers. The goal is to minimize inter-service coupling and avoid a "distributed monolith."

---

## Phase 1: Pragmatic Consolidation (Current to MVP)
*Focus: Speed, Simplicity, and Reduced Deployment Overhead.*

At this stage, having too many small services increases network latency and operational complexity. We group related functions into **"Macro-services."**

### 1. Identity & Profile Service
*   **Components**: `auth-service` + `user-service` + `vehicle-service`.
*   **Rationale**: These services all manage the "Actor" (User) and their assets (Vehicles). Combining them simplifies the registration and login flows.
*   **Responsibilities**: JWT issuance, profile management, driver/vehicle verification.

### 2. Marketplace Service (Trips & Rides)
*   **Components**: `trip-service` + `ride-service`.
*   **Rationale**: A **Ride** (Booking) is a segment of a **Trip**. Splitting them early forces complex distributed transactions to manage seat availability.
*   **Responsibilities**: Trip publishing, search/discovery, booking requests, seat management.

### 3. Notification Service
*   **Status**: Keep separate.
*   **Rationale**: It is a cross-cutting concern that is inherently asynchronous. Keeping it separate allows it to scale independently and use specialized providers (Firebase, Twilio).

---

## Phase 2: Strategic Scalability (Post-MVP Growth)
*Focus: Performance, Specialization, and Reliability.*

As the platform grows, we extract specific, high-load domains into dedicated services.

### 1. Trip Search Service
*   **Extract from**: Marketplace Service.
*   **Rationale**: Search is read-heavy and requires specialized technology (e.g., Elasticsearch) for geospatial matching.
*   **Tech Change**: Introduce an event-driven sync (Kafka) where `TripPublished` events are indexed in Search.

### 2. Payments & Financials
*   **New Service**: `payment-service`.
*   **Rationale**: Handles sensitive financial data, escrow, and driver payouts. This domain requires high security and strict audit logs.
*   **Pattern**: Implements Hexagonal Architecture to isolate business logic from external gateway (Stripe/MPESA) adapters.

### 3. Trust & Ratings
*   **Extract from**: User/Identity Service.
*   **Rationale**: Ratings and reviews are critical for community trust but follow a different lifecycle than user profiles. This service calculates reputations and handles disputes.

---

## Phase 3: Domain Maturity (BlaBlaCar Scale)
*Focus: Total Autonomy and Fine-grained Control.*

At "BlaBlaCar scale" (~1,500+ services), the architecture is refined into micro-segments.

### 1. Ridemap / GIS Service
*   Dedicated to calculating routes, detours, and matching pickups on a map (OpenStreetMap integration).

### 2. Fraud & Verification Service
*   ML-driven service to detect fake profiles, suspicious booking patterns, and automated scrapers.

### 3. Change Data Capture (CDC) Pipeline
*   Use of **Debezium + Kafka** to stream database changes to data warehouses (BigQuery) and other services, removing the need for direct HTTP polling.

---

## Service Mapping Summary

| Current Service | Recommended Grouping (Phase 1) | Future Domain (Phase 2+) |
| :--- | :--- | :--- |
| `auth-service` | **Identity Service** | Identity (Auth) |
| `user-service` | **Identity Service** | Profile / Trust |
| `vehicle-service` | **Identity Service** | Fleet / Logistics |
| `trip-service` | **Marketplace Service** | Trip Listing / Search |
| `ride-service` | **Marketplace Service** | Booking / Lifecycle |
| `notification-service` | **Notification Service** | Engagement / CRM |
| *(New)* | - | **Payment Service** |

---

## Consistency & Communication Rules

1.  **Prefer Asynchronous for Side-Effects**: If action A results in notification B, use an event/message broker.
2.  **Synchronous for Critical Intent**: Booking a seat (Marketplace) should be a synchronous API call to ensure immediate confirmation.
3.  **No Shared Databases**: Even if two services are conceptually "grouped," they should have their own schemas (logical separation) to make Phase 2 extraction easier.
4.  **Gateway-Driven**: All external traffic MUST go through the `api-gateway` for consistent auth/rate-limiting.
