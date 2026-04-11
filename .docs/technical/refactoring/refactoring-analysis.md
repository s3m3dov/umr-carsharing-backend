# Service Architecture Refactoring Analysis

**Date:** April 2026  
**Author:** Code Analysis  
**Version:** 1.0

---

## 1. Executive Summary

This document provides a comprehensive technical analysis of all 6 microservices in the carsharing-backend project, identifying code quality issues, architectural concerns, and specific refactoring recommendations prioritized by severity and effort.

### Services Analyzed

| Service | Port | Purpose | Endpoints |
|---------|------|---------|----------|
| auth-service | 8080 | Authentication, JWT, password management | 6 |
| user-service | 8081 | User profiles, drivers, vehicles | 24 |
| trip-service | 8082 | Trip booking, rides, management | 37 |
| review-service | 8083 | Reviews, ratings | 11 |
| notification-service | 8085 | Notifications, WebSocket | 5 |
| api-gateway | 8080 | Routing, auth filter | 1 + 5 routes |

---

## 2. Critical Issues (HIGH Severity)

### 2.1 Auth-Service: God Class - AuthService

**Location:** `auth-service/src/main/java/com/cimhub/authservice/service/AuthService.java`

**Issue:** Single class handles 6 distinct responsibilities:
- User registration with rollback
- Login authentication  
- JWT token generation/validation
- Password reset initiation
- Password reset confirmation
- Password change

**Current Implementation:**
```java
@Service
public class AuthService {
    public ApiResponseWrapper<LoginResponse> registerUser(RegisterUserRequest request) {
        // Creates user in auth-service
        // Validates license for DRIVER
        // Creates sub-profile in user-service
        // Handles rollback on failure
        // Generates JWT token
    }
    
    public ApiResponseWrapper<LoginResponse> login(LoginRequest request) {
        // Authenticates credentials
        // Generates JWT
        // Logs audit
    }
    
    public ApiResponseWrapper<ValidateTokenResponse> validateToken(ValidateTokenRequest request) {
        // Validates JWT tokens
    }
    
    // + 3 more password-related methods
}
```

**Impact:** Hard to test, maintain, and extend. Any change requires understanding all flows.

**Recommendation:** Split into 3 focused services:

```
service/
├── RegistrationService      # User creation, profile sync
├── AuthenticationService     # Login, JWT generation
└── PasswordManagementService # Reset, change password
```

---

### 2.2 User-Service: Code Duplication

**Location:** `user-service/src/main/java/com/cimhub/userservice/service/`

**Issue:** DriverService and PassengerService have nearly identical code:

```java
// DriverService.java - lines 45-52
private Driver findDriverOrThrow(String userId) {
    return driverRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + userId));
}

// PassengerService.java - lines 40-47 - IDENTICAL PATTERN
private Passenger findPassengerOrThrow(String userId) {
    return passengerRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Passenger not found: " + userId));
}
```

Also applies to:
- `createProfile()` methods - same pattern
- `updateProfile()` methods - same pattern
- `buildResponse()` methods - same structure

**Recommendation:** Create generic base service:

```java
// AbstractUserProfileService.java
public abstract class AbstractUserProfileService<T> {
    protected abstract Repository<T, String> getRepository();
    protected abstract ResourceNotFoundException notFoundException(String id);
    
    public T findOrThrow(String userId) {
        return getRepository().findByUserId(userId)
            .orElseThrow(() -> notFoundException(userId));
    }
}
```

---

### 2.3 User-Service: Vehicle Service Returns Null

**Location:** `user-service/src/main/java/com/cimhub/userservice/vehicle/service/VehicleService.java:27`

**Issue:** Returns null instead of throwing exception:

```java
public VehicleResponse registerVehicle(RegisterVehicleRequest request) {
    if (vehicleRepository.existsByUserId(request.userId())) {
        return null;  // WRONG: Should throw exception
    }
    // ...
}
```

**Impact:** NullPointerException downstream, inconsistent error handling.

**Recommendation:**
```java
public VehicleResponse registerVehicle(RegisterVehicleRequest request) {
    if (vehicleRepository.existsByUserId(request.userId())) {
        throw new UserAlreadyExistsException(
            "Vehicle already registered for user: " + request.userId()
        );
    }
    // ...
}
```

---

### 2.4 Trip-Service: TripGeospatialService God Class

**Location:** `trip-service/src/main/java/com/cimhub/tripservice/service/TripGeospatialService.java`

**Issue:** 404 lines, single method `findTripsMatchingRoute()` is 180+ lines:

```java
public List<TripResponse> findTripsMatchingRoute(
    String sourceAddress, 
    String destinationAddress,
    Integer availableSeats,
    LocalDate tripDate,
    String carType
) {
    // 180+ lines mixing:
    // - Time parsing
    // - In-memory filtering
    // - Geospatial queries
    // - Route matching
    // - Result mapping
}
```

**Impact:** Untestable, hard to debug, difficult to extend.

**Recommendation:** Split into focused services:
```
service/
├── route/
│   └── RouteMatchingService     # Route similarity algorithms
├── temporal/
│   └── DateRangeService       # Date/time filtering
└── geospatial/
    └── GeoQueryService         # MongoDB geospatial queries
```

---

### 2.5 Trip-Service: In-Memory Filtering

**Location:** `TripGeospatialService.findTripsMatchingRoute()` and `TripSearchService.searchAvailableTrips()`

**Issue:** Fetches all trips, filters in Java:

```java
// TripSearchService.java - lines 30-45
public Page<TripResponse> searchAvailableTrips(TripSearchCriteria criteria) {
    List<Trip> allTrips = tripsRepository.findAll();  // Fetches ALL
    return allTrips.stream()
        .filter(trip -> trip.getTripStatus() == TripStatus.AVAILABLE)  // In-memory!
        .map(this::toResponse)
        .collect(Collectors.collectingAndThen(...));
}
```

**Impact:** Does not scale. With 10,000 trips, fetches all 10,000.

**Recommendation:** Use MongoDB queries:

```java
public Page<TripResponse> searchAvailableTrips(TripSearchCriteria criteria) {
    return tripsRepository
        .findByTripStatusAndTripStartDateTimeUTCAfter(
            TripStatus.AVAILABLE,
            criteria.getDate(),
            criteria.getPageable()
        )
        .map(this::toResponse);
}
```

---

### 2.6 Trip-Service: N+1 Query

**Location:** `ActiveRidesService.convertToPassengerRideResponse():94`

**Issue:** Makes separate query per booking:

```java
public List<PassengerRideResponse> convertToPassengerRideResponse(
    List<Booking> bookings
) {
    return bookings.stream()
        .map(booking -> {
            Trip trip = tripsRepository.findByTripId(booking.getTripId()); // N+1!
            return buildResponse(trip, booking);
        })
        .collect(Collectors.toList());
}
```

**Recommendation:** Use batch query:

```java
public List<PassengerRideResponse> convertToPassengerRideResponse(
    List<Booking> bookings
) {
    List<String> tripIds = bookings.stream()
        .map(Booking::getTripId)
        .collect(Collectors.toList());
    
    Map<String, Trip> tripsMap = tripsRepository.findByTripIdIn(tripIds)
        .stream()
        .collect(Collectors.toMap(Trip::getTripId, t -> t));
    
    return bookings.stream()
        .map(b -> buildResponse(tripsMap.get(b.getTripId()), b))
        .collect(Collectors.toList());
}
```

---

### 2.7 Review-Service: Silent Failure

**Location:** `review-service/src/main/java/com/cimhub/reviewservice/service/TripServiceClient.java:50-53`

**Issue:** Returns false on errors, masks service unavailability:

```java
public boolean isTripCompleted(String tripId) {
    try {
        // Call trip-service
    } catch (RestClientException e) {
        log.error("Error calling trip-service for tripId: {}", tripId, e);
        return false;  // WRONG: Silently masks error
    }
}
```

**Impact:** Could allow review submission for non-completed trips.

**Recommendation:**
```java
public boolean isTripCompleted(String tripId) {
    try {
        // Call trip-service
    } catch (RestClientException e) {
        throw new ServiceUnavailableException(
            "Unable to validate trip status. Please try again."
        );
    }
}
```

---

### 2.8 Trip-Service: Duplicate Trip Fetch

**Location:** `CompletionService.completeTrip()` lines 93-94 and 126-128

**Issue:** Fetches same trip twice:

```java
public CompleteTripResponse completeTrip(String tripId, String driverId) {
    // First fetch - lines 93-94
    Trip trip = tripsRepository.findByTripId(tripId)
        .orElseThrow(() -> new TripNotFoundException(tripId));
    
    // ... lots of code ...
    
    // Second fetch - lines 126-128 (same trip!)
    Trip updatedTrip = tripsRepository.findByTripId(tripId)
        .orElseThrow(() -> new TripNotFoundException(tripId));
    
    return buildResponse(updatedTrip, completedBookings);
}
```

**Recommendation:**
```java
public CompleteTripResponse completeTrip(String tripId, String driverId) {
    Trip trip = tripsRepository.findByTripId(tripId)
        .orElseThrow(() -> new TripNotFoundException(tripId));
    
    // ... code ...
    
    // Use 'trip' variable directly or re-fetch only if needed
    return buildResponse(trip, completedBookings);
}
```

---

## 3. Medium Severity Issues

### 3.1 User-Service: Missing @Transactional

**Location:** `AdminDriverService.bulkApprove()`, `AdminDriverService.bulkReject()`

**Issue:** Multiple repository operations without transaction:

```java
public void bulkApprove(BulkDriverActionRequest request, String adminEmail) {
    for (String driverId : request.getDriverIds()) {
        Driver driver = driverRepository.findById(driverId).get();
        driver.setDriverStatus(DriverStatus.ACTIVE);
        driverRepository.save(driver);  // Each save is separate transaction
        auditService.log(...);
    }
    // If save fails mid-way, partial update remains
}
```

**Recommendation:**

```java
@Transactional
public void bulkApprove(BulkDriverActionRequest request, String adminEmail) {
    for (String driverId : request.getDriverIds()) {
        Driver driver = driverRepository.findById(driverId).get();
        driver.setDriverStatus(DriverStatus.ACTIVE);
        driverRepository.save(driver);
        auditService.log(...);
    }
}
```

---

### 3.2 User-Service: Deprecated Endpoint Still Exists

**Location:** `AdminPassengerController.createPassenger()` - lines 35-42

**Issue:** Endpoint exists but always throws exception:

```java
@PostMapping
public ApiResponseWrapper<PassengerResponse> createPassenger(
    @RequestBody CreatePassengerRequest request
) {
    throw new BadRequestException(
        "Passenger creation must be done through auth-service signup"
    );
}
```

**Impact:** Dead code, confusing API.

**Recommendation:** Remove endpoint entirely or implement properly.

---

### 3.3 Review-Service: Duplicated mapToResponse()

**Location:** `ReviewService.java:241-256` and `AdminReviewService.java:74-89`

**Issue:** Identical mapping code in two classes.

**Recommendation:** Extract to shared mapper:

```java
@Component
public class ReviewMapper {
    public ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
            .reviewId(review.getReviewId())
            // ...
            .build();
    }
}
```

---

### 3.4 Review-Service: Duplicated Average Rating

**Location:** `ReviewService.getDriverAverageRating()` and `getPassengerAverageRating()`

**Issue:** 90% identical code:

```java
// getDriverAverageRating() - lines 167-194
public AverageRatingResponse getDriverAverageRating(String driverId) {
    List<Review> reviews = repository.findByRevieweeIdAndRevieweeType(
        driverId, UserType.DRIVER);
    double avg = reviews.stream()
        .mapToInt(Review::getRating)
        .average()
        .orElse(0.0);
    return new AverageRatingResponse(driverId, avg, reviews.size());
}

// getPassengerAverageRating() - nearly identical with PASSENGER
```

**Recommendation:** Consolidate:

```java
public AverageRatingResponse getAverageRating(String userId, UserType userType) {
    List<Review> reviews = repository.findByRevieweeIdAndRevieweeType(userId, userType);
    double avg = reviews.stream()
        .mapToInt(Review::getRating)
        .average()
        .orElse(0.0);
    return new AverageRatingResponse(userId, avg, reviews.size());
}
```

---

### 3.5 Trip-Service: Inconsistent Exception Hierarchy

**Location:** `trip-service/src/main/java/com/cimhub/tripservice/exception/`

**Issue:** 10 custom exceptions, duplicates common patterns:

```
TripNotAvailableException       # Could use ResourceNotFoundException
TripNotInProgressException      # Could use BusinessException + ErrorType
RideNotCompletableException    # Could use BusinessException + ErrorType
// + 7 more
```

**Recommendation:** Use common module exceptions with ErrorType:

```java
// Instead of TripNotAvailableException
throw new ResourceNotFoundException(
    "Trip", 
    tripId, 
    ErrorType.BUSINESS_RULE_VIOLATION
);
```

---

### 3.6 Trip-Service: Unused Field

**Location:** `Trip.java` - `joinedBookingIds` field

**Issue:** Field defined but never used:

```java
@Field("joined_booking_ids")
private List<ObjectId> joinedBookingIds;  // Never referenced in code
```

**Recommendation:** Remove field or implement functionality.

---

### 3.7 Review-Service: In-Memory Rating Calculation

**Location:** `ReviewService.getDriverAverageRating()`

**Issue:** Fetches all reviews, calculates in memory:

```java
public AverageRatingResponse getDriverAverageRating(String driverId) {
    List<Review> reviews = repository.findByRevieweeId(driverId);
    double avg = reviews.stream()  // In-memory calculation
        .mapToInt(Review::getRating)
        .average()
        .orElse(0.0);
}
```

**Recommendation:** Use MongoDB aggregation:

```java
@Aggregation(pipeline = {
    "{ $match: { revieweeId: ?0 } }",
    "{ $group: { _id: null, avgRating: { $avg: '$rating' }, count: { $sum: 1 } } }"
})
AverageRatingResult getAverageRatingAgg(String revieweeId);
```

---

### 3.8 API-Gateway: Hardcoded Routes

**Location:** `api-gateway/src/main/resources/application.yml`

**Issue:** Routes hardcoded instead of using Kubernetes discovery:

```yaml
spring.cloud.gateway.routes:
  - id: user-service
    uri: http://user-service:8080  # Hardcoded
    predicates:
      - Path=/user-service/**
```

**Recommendation:** Use service discovery:

```yaml
spring.cloud.gateway:
  locator:
    enabled: true
    service-url:
      user-service: http://user-service:8080
```

---

## 4. Low Severity Issues

### 4.1 API-Gateway: Missing Circuit Breaker

**Issue:** No Resilience4j for upstream failures.

**Recommendation:** Add circuit breaker:

```java
@Bean
public CircuitBreakerRegistry circuitBreakerRegistry() {
    return CircuitBreakerRegistry.of(
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .build()
    );
}
```

---

### 4.2 API-Gateway: No Rate Limiting

**Issue:** Vulnerable to DDoS attacks.

**Recommendation:** Implement rate limiting filter:

```java
@Component
public class RateLimitingGatewayFilter implements GatewayFilter, Ordered {
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = exchange.getClientAddress().getAddress().getHostAddress();
        int count = requestCounts.computeIfAbsent(clientId, k -> new AtomicInteger(0))
            .incrementAndGet();
        
        if (count > MAX_REQUESTS_PER_SECOND) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
```

---

### 4.3 User-Service: Unused DTOs

**Location:** `user-service/src/main/java/com/cimhub/userservice/dto/`

**Issue:** DTOs not used:
- RegisterUserRequest
- LoginRequest  
- LoginResponse
- SignUpResponse

These belong to auth-service integration.

**Recommendation:** Remove unused DTOs or clarify service boundaries.

---

### 4.4 Trip-Service: OSRM DTOs Mixed

**Location:** `trip-service/src/main/java/com/cimhub/tripservice/dto/OSRM/`

**Issue:** OSRM DTOs in root dto package with business DTOs.

**Recommendation:** Create sub-package structure:

```
dto/
├── request/
│   ├── OfferRideRequest.java
│   └── JoinTripRequest.java
├── response/
│   ├── TripResponse.java
│   └── BookingResponse.java
└── osrm/
    ├── OsrmResponseDTO.java
    ├── Geometry.java
    └── Waypoint.java
```

---

### 4.5 TimezoneConversionService: Placeholder

**Location:** `trip-service/src/main/java/com/cimhub/tripservice/service/TimezoneConversionService.java`

**Issue:** Always returns "UTC":

```java
public String getTimeZoneId(Trip trip) {
    return "UTC";  // Always returns UTC - placeholder
}
```

**Recommendation:** Implement properly or remove.

---

### 4.6 API Examples Inconsistency

**Location:** All services - `*ApiExamples.java`

**Issue:** 
- Inconsistent location (some in `controller/`, some in `vehicle/controller/`)
- No standard interface
- Not linked to OpenAPI annotations

**Recommendation:** Create standard structure:

```
controller/
├── AuthController.java      # Main controller
└── AuthApiExamples.java    # Static constants, not a Spring bean
```

Better: Use `@ApiResponse` annotations directly:

```java
@Operation(
    summary = "User signup",
    responses = {
        @ApiResponse(
            responseCode = "201",
            description = "User created",
            content = @Content(schema = @Schema(implementation = SignUpResponse.class))
        )
    }
)
@PostMapping("/signup")
public ApiResponseWrapper<SignUpResponse> signup(@RequestBody RegisterUserRequest request) {
    // ...
}
```

---

## 5. Code Quality Patterns

### 5.1 Service Interface Pattern

**Current State:** No services implement interfaces.

**Recommendation:** Create interfaces for all services:

```java
// Interface
public interface RegistrationService {
    ApiResponseWrapper<LoginResponse> register(RegisterUserRequest request);
}

// Implementation
@Service
public class RegistrationServiceImpl implements RegistrationService {
    // ...
}
```

Benefits:
- Improved testability (mock interfaces)
- Clear contracts
- Enable alternative implementations

---

### 5.2 Generic Admin Service

**Current:** Separate AdminDriverService, AdminAdminVehicleService, AdminPassengerService with identical CRUD patterns.

**Recommendation:** Generic admin service:

```java
@Service
public class AdminService<T> {
    private final MongoRepository<T, String> repository;
    private final AuditService auditService;
    
    public void approve(String id, String adminEmail) {
        T entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(...));
        // Update status
        repository.save(entity);
        auditService.log(...);
    }
}
```

---

### 5.3 DTO Organization Standard

**Current:** Inconsistent DTO structure across services.

**Recommendation:** Standardized structure:

```
dto/
├── request/
│   ├── CreateRequest.java       # Base create DTO
│   ├── UpdateRequest.java       # Base update DTO
│   └── PaginationRequest.java   # Pagination params
├── response/
│   ├── ApiResponse.java          # Standard response wrapper
│   ├── ErrorResponse.java       # Standard error
│   └── paginated/
│       └── PageResponse.java   # Paginated response
└── {domain}/
    └── # Service-specific DTOs
```

---

## 6. Refactoring Priority Matrix

### Phase 1: Quick Wins (1-2 weeks)

| Priority | Task | Service | Lines to Change |
|----------|------|---------|-----------------|
| 1 | Add @Transactional | user-service | ~15 |
| 2 | Fix null return to exception | user-service (VehicleService) | ~3 |
| 3 | Remove deprecated endpoint | user-service | ~10 |
| 4 | Move OSRM DTOs to sub-package | trip-service | ~5 files |
| 5 | Extract service interfaces | All services | ~20 |

### Phase 2: Code Quality (2-4 weeks)

| Priority | Task | Service | Est. Effort |
|----------|------|---------|-------------|
| 1 | Split AuthService | auth-service | 3 days |
| 2 | Create generic AdminService | user-service | 2 days |
| 3 | Fix in-memory filtering | trip-service | 2 days |
| 4 | Fix N+1 query | trip-service | 1 day |
| 5 | Consolidate exceptions | trip-service | 2 days |
| 6 | Fix duplicate Trip fetch | trip-service | 1 day |

### Phase 3: Architectural (1-2 months)

| Priority | Task | Est. Effort |
|----------|------|-------------|
| 1 | Add circuit breaker (API-Gateway) | 3 days |
| 2 | Implement rate limiting | 2 days |
| 3 | Event-driven orchestration (trip-service) | 2 weeks |
| 4 | Review/Notification merge evaluation | 1 week |
| 5 | Vehicle service extraction evaluation | 1 week |

---

## 7. Summary Statistics

| Category | Count |
|----------|-------|
| Service Classes | 37 |
| Controllers | 21 |
| Custom Exceptions | 22 |
| DTOs | 64 |
| Repository Interfaces | 14 |
| MongoDB Collections | 7 |

### Issues by Severity

| Severity | Count |
|----------|-------|
| HIGH | 8 |
| MEDIUM | 10 |
| LOW | 6 |

---

## Appendix: Quick Reference

### Service Ports

| Service | Port |
|---------|------|
| api-gateway | 8080 |
| auth-service | 8080 |
| user-service | 8081 |
| trip-service | 8082 |
| review-service | 8083 |
| notification-service | 8085 |

### MongoDB Collections

| Service | Collection |
|---------|------------|
| auth-service | users, blacklisted_tokens |
| user-service | passengers, drivers, driver_documents, vehicles |
| trip-service | trips, bookings, vehicles |
| review-service | reviews, users |
| notification-service | notifications |

---

*Last Updated: April 2026*