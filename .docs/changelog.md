# Changelog

All notable changes to this project will be documented in this file.

The format is based on **Keep a Changelog**  
<https://keepachangelog.com/en/1.1.0/>  
and this project adheres to **Semantic Versioning**  
<https://semver.org/spec/v2.0.0.html>

---

---

## [0.0.4] – Standardization & System Refactoring

**Authors:**  

- Hikmat Samadov

### Added

- `ApiResponse` and `ApiError` abstractions for consistent API responses.
- Global exception handling with:
  - `BusinessException`
  - `GlobalExceptionHandler`
- Structured error codes (e.g. `TRIP_FULL`, `UNAUTHORIZED_ACCESS`).
- Reactive-compatible standardized responses across controllers.
- Dynamic OpenAPI / Swagger configuration with CORS support.

### Changed

- Domain model consolidation:
  - `Ride` + `Rider` → `Booking`
- Naming consistency refactor:
  - `Users` → `User`
  - `Trips` → `Trip`
  - `Rides` → `Ride`
- Introduced `BookingController` (passenger management, upcoming rides).
- Refined `TripController` responsibilities (search, join, lifecycle actions).
- Updated package namespace to `com.cimhub`.
- Renamed DTOs and services to match new domain language.
- Improved trip cancellation and joining logic.

### Removed

- Redundant Google Maven mirror configuration.
- Unused Docker templates and unnecessary compose version declarations.

---

## [0.0.3] – Core Feature Implementation & Service Hardening

**Authors:**  

- Vipul Singh

### Added

- Ride proximity search with 2D geospatial indexing.
- OpenStreetMap (OSM) route tracking and distance calculation.
- Booking, join, search, and cancellation endpoints.
- Passenger and driver cancellation flows.
- Health check endpoints for all microservices.
- Retry logic for Maven builds and service dependencies.

### Security

- Integrated Spring Security with JWT authentication.
- API Gateway authorization flow via Auth Service.
- Separated authentication logic from user management.
- Fixed hardcoded ports and security filter misconfigurations.

### Infrastructure

- Standardized inter-service communication over HTTP.
- Fixed CORS issues across API Gateway and Eureka.

---

## [0.0.2] – Microservices Transformation

**Authors:**  

- Vipul Singh

### Added

- Migration from monolith to microservices architecture.
- Eureka Discovery Server with Docker support.
- MongoDB Atlas integration (replacing local containerized MongoDB).
- Java 21 upgrade with updated Spring Cloud dependencies.
- Dockerfiles for all services.
- Executable Maven wrapper (`mvnw`).
- Initial `docker-compose.yml` for local orchestration.

---

## [0.0.1] – Foundation

**Authors:**  

- Vipul Singh

### Added

- Initial backend implementation for the Car Sharing platform.
- Core domain models and services.
- Project presentation materials and demo setup.

---
