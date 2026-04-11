# Changelog

All notable changes to this project will be documented in this file.

The format is based on **Keep a Changelog**
<https://keepachangelog.com/en/1.1.0/>
and this project adheres to **Semantic Versioning**
<https://semver.org/spec/v2.0.0.html>

---

## [Unreleased]

---

## [v0.9.3] – User Registration, Test Coverage & Refactoring (2026-04-11)

**Author:** s3m3dov (Hikmat Samadov)

### Fixed

- `GET /api/users/{userId}` returning null `firstName`, `lastName`, `phoneNumber` after signup — root cause was auth-service and user-service both writing to the same `users` MongoDB collection; auth-service saved a credentials-only document first, user-service found it and threw 409, auth-service silently ignored the conflict, leaving the null-field document in place forever
- Added classifier to spring-boot-maven-plugin to produce plain jar for dependencies
- Expanded actuator path exclusions to match wildcard patterns
- Removed redundant actuator path exclusions in API gateway
- Resolved compilation and test failures due to missing imports and constructor changes
- Improved validation annotations on DTOs and entities
- Added optimistic locking for seat booking concurrency
- Enforced identity and role-based access control
- Enforced vehicle uniqueness with exception-based flows
- Tightened profile validation and admin endpoint consistency
- Added default actuator path to `SecurityProperties`
- Adjusted actuator path configuration and cleaned up `SecurityProperties`
- Improved validation and exception handling in auth-service

### Changed

- **auth-service** credentials are stored in `users` and user-service is profile-only (`drivers`/`passengers`)
- **Signup flow** is fully server-side: auth-service creates auth user then role profile (`passenger` or `driver`) during `POST /api/auth/signup`; the Flutter client receives a JWT and is done — no follow-up calls required
- `licenseNumber` is stored exclusively in the `drivers` collection. `GET /api/users/{userId}` aggregates role profile data from `drivers`/`passengers`
- `POST /api/users/profile` removed; role profile creation uses `POST /api/passengers/{userId}` and `POST /api/drivers/{userId}`
- Reorganized service classes into domain-specific packages
- Reorganized service tests by domain packages
- Replaced OSM route service with configurable OSRM client
- Centralized trip and ride response mapping with `BookingMapper` and `TripMapper`
- Centralized controller role checks
- Validated and encapsulated route search inputs
- Improved notification wiring and configurability
- Split `AuthService` into focused services with utility classes for reusable functionality
- Migrated user-service to separate `Driver`/`Passenger` models
- Replaced Lombok `@Data` on persistence models with explicit accessors
- Extracted reusable profile update contracts
- Refactored review-service to reduce duplication and harden validation
- Standardized package naming conventions
- Moved OpenAPI example classes to `dto.examples` package
- Removed unused `Points` model from common module
- Updated references after package and class renames
- Refactored tests to use `Points.of()` factory method across trip-service

### Added

- Route geometry to trip, ride, and booking responses
- Service integration tests with grouped trip test structure
- Admin self profile endpoint in user-service
- Focused tests for low-coverage services
- Controller and config coverage tests for user-service
- Unit tests for `DriverService`, `PassengerService`, and `UserService`
- Test coverage improvements with fixtures and parameterized test methods
- Testing documentation with 89 test files
- Fixed request paths with response validation in HTTP tests
- `GET  /api/passengers/{userId}` — retrieve passenger profile
- `POST /api/passengers/{userId}` — initialize passenger sub-profile (called automatically during signup; also idempotent for manual use)
- `PUT  /api/passengers/{userId}` — update passenger name / phone / age
- `GET  /api/drivers/{userId}` — retrieve driver profile including `licenseNumber` and `driverStatus`
- `POST /api/drivers/{userId}` — initialize driver sub-profile with `licenseNumber` (called automatically during signup)
- `PUT  /api/drivers/{userId}` — update driver name / phone / age / license number
- Driver sub-profile auto-created on first GET if missing (lazy creation fallback)
- Auth-service rolls back the auth `users` document if user-service profile creation fails, keeping both stores consistent

### Security

- Admin signup hardening with expanded analysis

### Documentation

- Updated references after package and class renames
- Noted admin role signup hardening analysis

---

## [v0.9.2] – Trip Start, Retrieval & Test Coverage (2026-04-06)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Trip start workflow support with invalid transition handling
- Booking and trip retrieval endpoints in trip-service
- Additional unit and integration tests for booking and trip retrieval flows

### Changed

- Refactored completion workflows with detailed response DTOs
- Consolidated booking, completion, and cancellation tests into a single trip test script

---

## [v0.9.1] – Testing & Enhancements (2026-04-05)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Comprehensive unit tests for service layers (bookings, trips, cancellations)
- Unit tests for `AuthService`, `JoinTripService`, and `VehicleService`
- Comprehensive HTTP tests for notifications, admin actions, and trip operations
- Unit tests for `GatewayExceptionHandler` with enhanced coverage
- Logback test configuration and aggregate code coverage report
- `BookingAlreadyProcessedException` for enhanced booking status validations
- Driver-accept flow for booking requests with enhanced auto-confirm logic
- Enhanced vehicle registration response with detailed vehicle data
- Health and static asset paths to public paths in gateway security config
- Consolidated `minPrice` and `maxPrice` filters in trip query

### Changed

- Updated route geometry to use `Geometry` object instead of string
- Enhanced HTTP test coverage with additional validations for notifications and trips
- Updated token validation tests to reflect 401 responses for invalid tokens
- Updated duplicate vehicle response to HTTP 409 with enhanced tests
- Updated API reference with gateway routing, new endpoints, and response formats
- Expanded README documentation with repository structure and setup guide
- Updated resource limits, enabled Grafana subpath access, and added ingress configuration for monitoring
- Enhanced exception handling and logging in `GatewayExceptionHandler`

### Removed

- Unused security tests and outdated test configurations

### Fixed

- Enhanced JWT error handling in security configuration

---

## [v0.9.0] – Monitoring, Admin Panel & Audit Logging (2026-03-30)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- **Kubernetes monitoring stack** with Prometheus, Grafana, Loki, and Fluent Bit
- **Observability instrumentation** across services:
  - Prometheus metrics support
  - OTLP tracing support
  - JSON-structured Logback logging
- **Admin panel features** across microservices:
  - User and vehicle management endpoints in user-service
  - Trip, booking, and reporting endpoints in trip-service
  - Review management endpoints in review-service
  - Shared admin repositories/models and common admin components
  - Centralized admin authorization at API gateway level
  - Configurable pagination settings for admin APIs
- **Audit logging platform**:
  - Typed audit infrastructure in common module
  - Audit logging integrated into auth-service, user-service, trip-service, and review-service
  - Audit log report endpoint in trip-service
- Logout functionality in auth-service with token blacklisting
- Documentation:
  - Admin panel architecture documentation
  - Comprehensive audit logging documentation
  - Kubernetes monitoring guide and docs index updates

### Fixed

- Corrected admin path patterns in API gateway authorization routing

---

## [v0.8.0] – Service Enhancements & Documentation (2026-03-30)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- **Trip-service features:**
  - `RideController` for passenger ride management with per-seat pricing model
  - Passenger ride history endpoint (completed and cancelled rides)
  - Driver trip history endpoint (completed and cancelled trips)
  - `carType` attribute on trips with search filtering (`carType`, `minPrice`, `maxPrice`, `minAvailableSeats`)
  - `@Min` validation for `totalSeats` in `OfferRideRequest`
- `spring-boot-starter-validation` dependency across services
- OpenAPI documentation improvements:
  - Annotations and example payloads across all DTOs and controllers
  - JWT `Bearer Authentication` security scheme in all service OpenAPI configs
  - Standardized error response documentation across services
  - Springdoc OpenAPI dependency added to common module
- Documentation:
  - Comprehensive API reference and documentation approach
  - Domain glossary with trip vs ride terminology table
  - BlaBlaCar system architecture overview
  - Phased microservice organization strategy (consolidation → scalability → maturity)
  - GCP Initial Setup Guide with revised cluster and project configurations
  - Phase 1 TODO document for core logic improvements
  - GitOps script documentation with context-aware `local`/`gcp` command structure

### Changed

- Renamed `ApiResponse` → `ApiResponseWrapper` for clarity
- Renamed `licensePlate` → `licenseNumber` across all services and documentation
- Renamed `OFFERED` trip status → `CREATED` across trip-service
- Renamed `driverId` → `subjectId` in review-service assertions for model consistency
- Migrated entity annotations to JPA (`jakarta.persistence-api 3.1.0`) for database-agnostic design, then reverted review-service and trip-service back to MongoDB
- Reordered GCP workflow steps; enhanced deployment script with pod cleanup logic
- Unified `gitops.sh` command structure across local and GCP scripts with automatic context switching
- Updated JWT validation logic with `AuthServiceResponse` mapping
- Consolidated documentation structure and reorganized technical docs
- Consolidated vehicle-service and ride-service details into user-service and trip-service docs
- Updated review-service error types in `ReviewErrorCode` for improved categorization
- Updated review-service Dockerfile: base images, runtime port to 8084, CA certificates for MongoDB Atlas

### Removed

- `ride-service` and `vehicle-service` modules (consolidated into existing services)
- Unused security item from OpenAPI config in auth-service
- Outdated `ReviewServiceTest`
- Detailed database-agnostic setup documentation (superseded)

### Fixed

- Trailing whitespace in `ApiError` JavaDoc

---

## [v0.7.0] – Review Service & Identity Enhancements (2026-03-30)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- **review-service** microservice with MongoDB support, mutual driver-passenger reviews, 7 endpoints, unit tests, gateway routing, and Kubernetes manifests
- **Password management** in auth-service:
  - Password reset functionality with new endpoints, DTOs, and service logic
  - Change password endpoint with enhanced password validation
  - `AuthExceptionHandler` for `BadCredentialsException` with improved error handling in `SecurityConfig`
- **Role-based user model** in user-service:
  - `Driver` / `Passenger` subclasses replacing flat `User` model
  - `firstName` / `lastName` split from `fullName`
  - Driver-specific fields (`vehicleModel`, `licenseNumber`) isolated to subclass
- **Auth-service identity enhancements:**
  - `userType` persisted on auth `User` entity and embedded as JWT role claim
  - Signup now calls user-service to create profile in a single request (via `RestClient`)
  - Conflict prevention for existing user profiles during signup
- **API Gateway** re-enabled JWT enforcement with `X-User-Role` header propagation to downstream services

---

## [v0.6.0] – Trip Lifecycle & Identity Fields (2026-01-11)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- `TripStatus` enum introduced and integrated across services (including join flow behavior)
- `UserType` field added to profile creation and login response; included in `UserResponse`
- Swagger annotations for trip start date-time fields (request + response models)
- Trip search enhancements:
  - Improved parameter naming
  - Optional time-range filtering
  - Additional validation

### Changed

- Renamed `UpcomingRidesService` → `ActiveRidesService` and updated related methods
- Simplified `ApiResponse` success message to a static standard message
- OpenAPI server descriptions updated to remove "Development" suffix
- Deprecated user-service endpoints commented (kept for reference)
- Script naming clarified:
  - `build-for-kind.sh` → `build-kind.sh`
  - `setup-gcp.sh` → `connect-gcp.sh`

### Fixed

- Fixed empty booking response for passengers

---

## [v0.5.1] – GCP Deployment & Request Tracing (2026-01-05 to 2026-01-07)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Request ID propagation across gateway and services:
  - `RequestIdFilter` for services
  - `RequestIdGatewayFilter` for API gateway
- Initial GCP/GKE deployment support:
  - Deployment scripts for cloud environment
  - GCP-specific documentation
  - GitOps configuration updates for cloud deployments
- Docker build improvements:
  - Registry migration from `gcr.io` → `eu.gcr.io`
  - Added `linux/amd64` platform support for cross-platform builds
  - Standardized image paths across deployments and kustomization overlays

### Changed

- GitOps scripts refactored:
  - Centralized common utilities into `common.sh`
  - Removed redundant helper files
- Error response standardization:
  - Introduced/refined `ErrorBody`, `ErrorDetails`, `ErrorDetail` models
  - Standardized usage of `ServiceErrorCode` across all services
  - Improved `HttpMessageNotReadableException` error details
- Ingress and networking configuration:
  - Enabled and tuned forward headers for proxy support
  - Updated ingress class configuration to GCE
  - CORS configuration updates for cloud environment
- Scaling adjustments:
  - Reduced replica counts for cost optimization
  - Temporarily disabled some non-essential deployments

### Security

- JWT authorization enforcement temporarily disabled in `JwtValidationFilter` (explicitly marked as temporary for development purposes)

---

## [v0.5.0] – Kubernetes Migration (2026-01-03 to 2026-01-04)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Kubernetes-first deployment approach for both local (Kind) and production environments
- GitOps-oriented structure for manifest management and secrets workflow
- Service accounts for all microservices
- Comprehensive health probes (liveness and readiness) with updated probe paths
- Centralized security configuration via `SecurityProperties`

### Changed

- Standardized all service ports to **8080**
- Migrated all services to Kubernetes with consistent service types (ClusterIP/LoadBalancer)
- Optimized resource requests and limits across all services
- Updated API Gateway routing for Kubernetes networking
- OpenAPI configuration adjusted for Kubernetes service discovery
- Security configuration refactored:
  - Improved public/docs/actuator path matching logic
  - Enhanced actuator sub-path handling
  - Simplified JWT validation filter with optimized path checks

### Removed

- Eureka-specific service discovery dependencies and configuration
- Legacy Docker Compose-only deployment approach

---

## [v0.4.3] – Documentation & Roadmap (2026-01-02)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Phase 1 task list and development checklist documentation
- Detailed roadmap with structured phases and deliverables
- Comprehensive README with organized documentation links
- This changelog file

### Changed

- Reorganized documentation directories and structure
- Updated documentation links to reflect new repository layout

---

## [v0.4.2] – Local Development Workflow (2026-01-01)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Dockerfile configurations using supervisord for service deployment
- Improved local development experience with Docker Compose

### Changed

- Renamed `build.sh` → `run-compose.sh` for clarity
- Application configuration updates:
  - Enabled load balancer retry mechanisms
  - Adjusted Eureka registry fetch intervals

---

## [v0.4.1] – API Standardization (2025-12-31)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Standardized `ApiResponse` model for consistent success responses
- Standardized `ApiError` model for consistent error responses
- Global exception handling with improved consistency across all services

### Changed

- Domain model consolidation:
  - Introduced `Booking` to consolidate ride/rider-related entities
- Major naming consistency refactor:
  - `Users` → `User`
  - Aligned `Rides`/`Trips` nomenclature to `Ride`/`Trip`
- Refactored and expanded controllers for booking and trip operations
- Reorganized package structure for better maintainability
- Enhanced exception handling patterns across services

---

## [v0.4.0] – OpenAPI Integration & Configuration (2025-12-30)

**Author:** s3m3dov (Hikmat Samadov)

### Added

- Springdoc OpenAPI dependencies and configuration across all services
- Dynamic OpenAPI documentation generation
- CORS support for API documentation endpoints

### Changed

- Standardized namespace to `com.cimhub` across entire codebase
- MongoDB URI configuration migrated to environment variables
- Docker Compose improvements:
  - Enhanced environment variable handling with example `.env` file
  - Improved healthcheck formatting and reliability
- Docker build optimizations with better layer caching
- Updated documentation for microservices architecture and local setup

### Removed

- Redundant Google Maven mirror configuration from Dockerfiles
- Unused Docker templates and unnecessary compose file declarations
- Legacy version declarations from docker-compose.yml

---

## [v0.3.0] – Core Ride Features & Security (2025-09-28 to 2025-10-31)

**Author:** VipulSingh-10

### Added

- Ride proximity search with MongoDB geospatial indexing
- OpenStreetMap (OSM) and OSRM integration for route tracking
- Distance calculation and route optimization
- Comprehensive booking workflow:
  - Driver: offer ride, view bookings, cancel rides
  - Passenger: search rides, join rides, cancel bookings
- Health check endpoints across all microservices

### Changed

- Standardized inter-service communication to HTTP-based approach
- Migrated from containerized MongoDB to MongoDB Atlas for production
- Docker and docker-compose configuration updates for improved local orchestration

### Security

- Integrated Spring Security framework with JWT authentication
- Implemented API Gateway authorization flow via Auth Service
- Fixed security filter misconfigurations and hardcoded port issues

### Infrastructure

- Added retry logic for Maven builds to handle transient failures
- Service dependency handling improvements in Docker Compose
- CORS configuration fixes across gateway and service ecosystem

---

## [v0.2.0] – Microservices Architecture (2025-06-18)

**Author:** VipulSingh-10

### Added

- Complete migration from monolithic to microservices architecture
- Netflix Eureka Discovery Server for service registration and discovery
- Docker support for all microservices
- Service-to-service communication patterns

### Changed

- Decomposed monolithic application into independent microservices:
  - User Service
  - Vehicle Service
  - Trip Service
  - Auth Service
  - API Gateway
  - Discovery Server (Eureka)

---

## [v0.1.0] – Initial Release (2025-06-16)

**Author:** VipulSingh-10

### Added

- Initial backend implementation with core functionality
- Domain models for users, vehicles, and trips
- Basic REST API endpoints
- MongoDB integration
- Demo and presentation materials

### Fixed

- Made `mvnw` (Maven Wrapper) executable on Unix systems
