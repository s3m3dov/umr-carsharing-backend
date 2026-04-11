# Project Folder Structure Standard

**Purpose:** Document and standardize the folder structure for all microservices in carsharing-backend  
**Scope:** All services: auth, user, trip, review, notification, api-gateway  
**Version:** 1.0

---

## 1. Current State vs Recommended

### 1.1 Current Flat Structure (Problematic)

```
auth-service/src/main/java/com/cimhub/authservice/
├── AuthServiceApplication.java
├── config/                      # Mixed - security, openapi, restclient
├── controller/
│   ├── AuthController.java
│   └── AuthApiExamples.java     # NOT a controller - place in dto/examples/
├── dto/
│   ├── request/                 # OK
│   └── response/                # OK
├── exception/
│   └── AuthExceptionHandler.java # Exception handler, not service - place in exception/
├── model/                       # OK - entities
├── repository/                  # OK
├── security/                   # Should be in config/
├── service/                    # OK
└── (root)                    # Root package - AuthServiceApplication here
```

### 1.2 Recommended Structure

```
service-name/src/main/java/com/cimhub/{service}/
├── {ServiceName}Application.java    # Main entry point at root
│
├── config/                            # ALL configuration classes
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   ├── WebClientConfig.java
│   ├── DatabaseConfig.java
│   └── {Feature}Properties.java
│
├── controller/                       # REST controllers ONLY
│   ├── {Domain}Controller.java
│   ├── {Domain}AdminController.java
│   └── {Domain}InternalController.java
│
├── dto/
│   ├── request/                       # Request DTOs
│   │   ├── auth/
│   │   ├── trip/
│   │   └── common/
│   │       ├── PaginationRequest.java
│   │       └── FilterRequest.java
│   │
│   ├── response/                     # Response DTOs
│   │   ├── auth/
│   │   ├── trip/
│   │   └── common/
│   │       └── ApiResponseWrapper.java
│   │
│   ├── examples/                     # OpenAPI documentation (NOT Spring beans)
│   │   ├── AuthApiDoc.java
│   │   ├── TripApiDoc.java
│   │   └── ErrorExamples.java
│   │
│   └── osrm/                        # External API DTOs
│       ├── OsrmResponse.java
│       ├── Geometry.java
│       └── Waypoint.java
│
├── exception/                        # Exception handlers & custom exceptions
│   ├── GlobalExceptionHandler.java
│   ├── {Domain}Exception.java
│   └── {Domain}ErrorCode.java
│
├── model/                           # Domain entities (MongoDB documents)
│   ├── User.java
│   ├── Trip.java
│   └── Booking.java
│
├── repository/                      # Data access layer
│   ├── UserRepository.java
│   ├── TripRepository.java
│   └── custom/
│       └── CustomTripRepository.java
│
├── service/                        # Business logic (keep FLAT)
│   ├── AuthService.java
│   ├── TripService.java
│   └── helper/
│       ├── {Domain}Mapper.java
│       └── {Domain}Validator.java
│
└── security/                      # DEPRECATED - move to config/
```

---

## 2. Standard Package Definitions

### 2.1 Package Purpose Table

| Package | Contents | Contains Spring Beans |
|---------|----------|---------------------|
| `config/` | Configuration classes | `@Configuration`, `@Bean` methods |
| `controller/` | REST endpoints | `@RestController`, `@RequestMapping` |
| `dto/request/` | Request DTOs | Records, validation annotations |
| `dto/response/` | Response DTOs | Records, JSON annotations |
| `dto/examples/` | OpenAPI examples | Static constants ONLY |
| `dto/osrm/` | External API DTOs | Third-party integrations |
| `exception/` | Exception classes | `@ControllerAdvice`, custom exceptions |
| `model/` | Domain entities | `@Document`, JPA entities |
| `repository/` | Data access | `Repository` interfaces |
| `service/` | Business logic | `@Service` classes |
| `security/` | DEPRECATED | Use `config/security/` |

### 2.2 What Goes Where

| Component Type | Correct Package | Incorrect Package |
|----------------|------------------|--------------------|
| `@Configuration` class | `config/` | Root or any other |
| `@RestController` | `controller/` | Root or dto/ |
| Request DTO | `dto/request/` | dto/ root |
| Response DTO | `dto/response/` | dto/ root |
| OpenAPI examples | `dto/examples/` | controller/ |
| Exception handler | `exception/` | controller/ or service/ |
| Custom exception | `exception/` | Root |
| Entity/Document | `model/` | Root or dto/ |
| Repository | `repository/` | Root or model/ |
| Service | `service/` | Root or controller/ |

---

## 3. Service-Specific Structures

### 3.1 Auth-Service (Current)

```
auth-service/src/main/java/com/cimhub/authservice/
├── AuthServiceApplication.java          # OK - root
├── config/                             # OK
│   ├── OpenApiConfig.java
│   ├── RestClientConfig.java
│   ├── SecurityConfig.java
│   └── SecurityProperties.java
├── controller/                         # OK
│   ├── AuthController.java
│   └── AuthApiExamples.java          # WRONG - move to dto/examples/
├── dto/
│   ├── request/
│   │   ├── ChangePasswordRequest.java
│   │   ├── ConfirmResetPasswordRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterUserRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   └── ValidateTokenRequest.java
│   └── response/
│       ├── LoginResponse.java
│       ├── ResetPasswordResponse.java
│       ├── SignUpResponse.java
│       └── ValidateTokenResponse.java
├── exception/                         # PARTIAL - ExceptionHandler OK, rest wrong
│   └── AuthExceptionHandler.java
├── model/
│   ├── BlacklistedToken.java
│   └── User.java
├── repository/
│   ├── AuthUserRepository.java
│   └── BlacklistedTokenRepository.java
├── security/                         # WRONG - move to config/
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
└── service/
    ├── AuthService.java
    ├── CustomUserDetailService.java
    └── UserServiceClient.java
```

**Auth-Service Recommended Changes:**
```
# Move files:
security/           → config/
controller/AuthApiExamples.java → dto/examples/AuthApiDoc.java
```

---

### 3.2 Trip-Service (Current)

```
trip-service/src/main/java/com/cimhub/tripservice/
├── TripServiceApplication.java          # OK
├── config/                             # OK
│   ├── MongoConfig.java
│   ├── OpenApiConfig.java
│   └── OSMClientConfig.java
├── controller/                         # OK
│   ├── TripController.java
│   ├── TripApiExamples.java         # WRONG
│   ├── BookingController.java
│   ├── BookingApiExamples.java      # WRONG
│   ├── RideController.java
│   ├── ReportsController.java
│   ├── AdminTripController.java
│   └── AdminBookingController.java
├── dto/
│   ├── request/
│   ├── response/                    # OK - but OSRM mixed here
│   │   ├── BookingResponse.java
│   │   ├── TripResponse.java
│   │   └── ...
│   └── OSRM/                       # OK - sub-package
│       ├── Geometry.java
│       ├── Waypoint.java
│       ├── Route.java
│       ├── OsrmResponseDTO.java
│       └── Legs.java
├── exception/                        # OK - but too many exceptions
│   ├── TripErrorCode.java
│   ├── TripNotAvailableException.java
│   └── ... (10 more)
├── model/
│   ├── Trip.java
│   ├── Booking.java
│   ├── TripStatus.java
│   ├── BookingStatus.java
│   ├── VehicleDocument.java
│   └── GeoJsonPoint.java
├── repository/
│   ├── TripsRepository.java
│   ├── BookingRepository.java
│   └── VehicleDocumentRepository.java
├── service/
│   ├── TripService.java
│   ├── TripSearchService.java
│   ├── TripGeospatialService.java    # TOO BIG - split
│   ├── OfferRideService.java
│   ├── JoinTripService.java
│   ├── BookingService.java
│   ├── CompletionService.java
│   ├── CancellationService.java
│   ├── ActiveRidesService.java
│   ├── ReportsService.java
│   ├── DriverNotificationService.java
│   ├── AdminTripService.java
│   ├── AdminBookingService.java
│   ├── TimezoneConversionService.java
│   └── routes/
│       └── OSMRoute.java          # WRONG - should be in service/helper/
```

**Trip-Service Recommended Changes:**
```
# Move:
controller/TripApiExamples.java     → dto/examples/TripApiDoc.java
controller/BookingApiExamples.java → dto/examples/TripApiDoc.java
service/routes/OSMRoute.java       → service/helper/OsrmHelper.java

# Split:
service/TripGeospatialService.java → service/geospatial/GeoQueryService.java
                          + service/temporal/DateRangeService.java
                          + service/route/RouteMatchingService.java
```

---

### 3.3 User-Service (Current)

```
user-service/src/main/java/com/cimhub/userservice/
├── UserServiceApplication.java
├── config/
│   └── OpenApiConfig.java
├── controller/
│   ├── UserController.java
│   ├── UserApiExamples.java       # WRONG
│   ├── DriverController.java
│   ├── PassengerController.java
│   ├── AdminDriverController.java
│   ├── AdminPassengerController.java
│   └── vehicle/
│       ├── VehicleController.java
│       ├── AdminVehicleController.java
│       ├── VehicleApiExamples.java  # WRONG - nested
│       ├── model/
│       │   └── Vehicle.java
│       ├── service/
│       │   ├── VehicleService.java
│       │   └── AdminVehicleService.java
│       ├── repository/
│       │   └── VehicleRepository.java
│       └── dto/
│           ├── request/
│           └── response/
```

**User-Service Recommended Changes:**
```
# Move:
controller/UserApiExamples.java          → dto/examples/UserApiDoc.java
vehicle/VehicleApiExamples.java   → dto/examples/VehicleApiDoc.java

# Decision on vehicle/:
# Option A: Keep as sub-package (OK if vehicle is bounded context)
# Option B: Extract to separate service (if vehicle grows)
```

---

## 4. Cross-Service Comparison

### Current Structure Comparison

| Package | auth | user | trip | review | notification | api-gateway |
|---------|------|------|------|--------|--------------|--------------|
| `config/` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| `controller/` | Mixed* | Mixed* | Mixed* | ✓ | ✓ | ✓ |
| `dto/request/` | ✓ | ✓ | ✓ | ✓ | ✓ | N/A |
| `dto/response/` | ✓ | ✓ | Mixed* | ✓ | ✓ | N/A |
| `dto/examples/` | ✗ | ✗ | ✗ | ✗ | ✗ | N/A |
| `dto/osrm/` | N/A | N/A | ✓ | N/A | N/A | N/A |
| `exception/` | Partial | ✓ | ✓ | ✓ | ✓ | ✓ |
| `model/` | ✓ | ✓ | ✓ | ✓ | ✓ | N/A |
| `repository/` | ✓ | ✓ | ✓ | ✓ | ✓ | N/A |
| `service/` | ✓ | ✓ | ✓ | ✓ | ✓ | N/A |
| `security/` | ✗ | N/A | N/A | N/A | N/A | ✗ |

*Mixed* = Examples mixed in controller, OSRM DTOs in response

**Key:**
- ✓ = Correct
- ✗ = Needs correction
- N/A = Not applicable

---

## 5. Migration Checklist

### Phase 1: Folder Organization

| Task | Service | Priority |
|------|--------|----------|
| Move `security/` → `config/` | auth-service | HIGH |
| Move `*ApiExamples.java` → `dto/examples/*ApiDoc.java` | All | HIGH |
| Move `service/routes/` → `service/helper/` | trip-service | MEDIUM |
| Move OSRM DTOs to `dto/osrm/` | trip-service | LOW |

### Phase 2: Code Organization

| Task | Service | Priority |
|------|--------|----------|
| Split TripGeospatialService (3+ services) | trip-service | HIGH |
| Remove duplicate exceptions | trip-service | MEDIUM |
| Consolidate exception handlers | All | MEDIUM |

### Phase 3: Service Extraction

| Task | Service | Priority |
|------|--------|----------|
| Evaluate vehicle extraction | user-service | MEDIUM |
| Evaluate review+notification merge | review, notification | LOW |

---

## 6. Quick Reference

### 6.1 Standard Service Template

```
src/main/java/com/cimhub/{service}/
├── {Service}Application.java      # Main entry
├── config/                     # ALL @Configuration
│   ├── {Feature}Config.java
│   └── {Feature}Properties.java
├── controller/                  # ALL @RestController
│   ├── {Domain}Controller.java
│   └── {Domain}AdminController.java
├── dto/
│   ├── request/
│   │   └── {Action}{Entity}Request.java
│   ├── response/
│   │   └── {Entity}Response.java
│   ├── examples/
│   │   └── {Domain}ApiDoc.java
│   └── {external}/
│       └── # External API DTOs
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── {Concept}Exception.java
├── model/
│   └── {Entity}.java
├── repository/
│   ├── {Entity}Repository.java
│   └── custom/
│       └── Custom{Entity}Repository.java
└── service/
    ├── {Domain}Service.java
    └── helper/
        └── {Helper}Util.java
```

### 6.2 File Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Main app | `{Service}Application.java` | `AuthServiceApplication.java` |
| Configuration | `{Purpose}Config.java` | `SecurityConfig.java` |
| Controller | `{Domain}Controller.java` | `TripController.java` |
| Admin controller | `{Domain}AdminController.java` | `TripAdminController.java` |
| Request DTO | `{Action}{Entity}Request.java` | `CreateTripRequest.java` |
| Response DTO | `{Entity}Response.java` | `TripResponse.java` |
| API Examples | `{Domain}ApiDoc.java` | `TripApiDoc.java` |
| Exception | `{Concept}Exception.java` | `TripNotFoundException.java` |
| Entity | `{Entity}.java` | `Trip.java` |
| Repository | `{Entity}Repository.java` | `TripRepository.java` |
| Service | `{Domain}Service.java` | `TripService.java` |

---

## 7. Anti-Patterns to Avoid

### 7.1 What NOT to Do

| Anti-Pattern | Why | Correct |
|-------------|-----|---------|
| Examples as Spring bean | Unnecessary overhead | Static class in dto/examples/ |
| Security config in security/ | Scattered config | All in config/ |
| Routes in service/routes/ | Inconsistent with helper/ | service/helper/ |
| Exceptions in root | Hard to find | exception/ package |
| Multiple root-level classes | No organization | Proper packages |
| Nested packages for equals concern | Overstructuring | Flat structure |

### 7.2 Good Examples

```
✓ dto/examples/AuthApiDoc.java with nested static classes
✓ config/SecurityConfig.java
✓ exception/GlobalExceptionHandler.java
✓ service/helper/OsrmHelper.java
```

### 7.3 Bad Examples

```
✗ controller/AuthApiExamples.java (not a controller)
✗ security/JwtService.java (not security-related config)
✗ service/routes/OSMRoute.java (not a service)
✗ AuthService.java at root (no package)
```

---

*Last Updated: April 2026*