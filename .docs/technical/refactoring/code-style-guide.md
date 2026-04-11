# Code Style & Readability Guidelines

**Purpose:** Ensure consistent code quality across all microservices  
**Scope:** All Java code in carsharing-backend  

---

## 1. Naming Conventions

### 1.1 Classes & Interfaces

| Type | Convention | Example |
|------|-------------|---------|
| Service | `{Domain}Service` | `TripService`, `AuthService` |
| Service Interface | `I{Domain}Service` or `{Domain}Service` (interface) | `RegistrationService` |
| Controller | `{Domain}Controller` | `TripController`, `BookingController` |
| Repository | `{Entity}Repository` | `TripRepository`, `BookingRepository` |
| Model/Entity | Single noun, PascalCase | `Trip`, `Booking`, `User` |
| Exception | `{Concept}Exception` | `TripNotFoundException` |
| DTO Request | `{Action}{Entity}Request` | `CreateTripRequest`, `UpdateUserRequest` |
| DTO Response | `{Entity}Response` | `TripResponse`, `UserResponse` |
| Config | `{Purpose}Config` | `SecurityConfig`, `MongoConfig` |

### 1.2 Methods

| Type | Convention | Example |
|------|-------------|---------|
| Command (write) | Verb + noun | `createTrip()`, `updateUser()`, `deleteBooking()` |
| Query (read) | `find` + noun / `get` + noun | `findTrips()`, `getUserById()` |
| Boolean | `is` + concept / `has` + concept / `can` + action | `isActive()`, `hasPermission()` |
| Private helper | `_` prefix (optional) | `_buildResponse()`, `_validateInput()` |

### 1.3 Variables

| Type | Convention | Example |
|------|-------------|---------|
| Local | camelCase | `tripId`, `userEmail` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| Boolean | `is`/`has`/`can` prefix | `isValid`, `canDelete` |
| Collection | Plural noun or suffix `List`/`Map` | `trips`, `userIds` |

---

## 2. Class Structure

### 2.1 Standard Service Class

```java
/**
 * Service for managing trip lifecycle operations.
 * Handles creation, status updates, and completion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    // Dependencies - constructor injected
    private final TripsRepository tripsRepository;
    private final BookingRepository bookingRepository;
    private final AuditService auditService;

    // ========== PUBLIC API ==========

    /**
     * Creates a new trip offer.
     *
     * @param request trip creation request
     * @return created trip with generated ID
     * @throws InvalidRequestException if validation fails
     */
    public TripResponse createTrip(CreateTripRequest request) {
        validateRequest(request);
        Trip trip = buildTripEntity(request);
        Trip saved = tripsRepository.save(trip);
        auditService.log(AuditAction.CREATE, saved.getTripId());
        return toResponse(saved);
    }

    /**
     * Updates trip status with validation.
     *
     * @param tripId target trip
     * @param newStatus desired status
     * @throws ResourceNotFoundException if trip not found
     */
    @Transactional
    public void updateStatus(String tripId, TripStatus newStatus) {
        Trip trip = findOrThrow(tripId);
        validateStatusTransition(trip.getStatus(), newStatus);
        trip.setStatus(newStatus);
        tripsRepository.save(trip);
    }

    // ========== PRIVATE HELPERS ==========

    private Trip findOrThrow(String tripId) {
        return tripsRepository.findByTripId(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));
    }

    private void validateStatusTransition(TripStatus from, TripStatus to) {
        // Implementation
    }

    private Trip buildTripEntity(CreateTripRequest request) {
        // Implementation
    }

    private TripResponse toResponse(Trip trip) {
        // Implementation
    }
}
```

### 2.2 Standard Controller

```java
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Api(tags = "Trip Management")
public class TripController {

    private final TripService tripService;
    private final TripSearchService searchService;

    @PostMapping
    @ApiOperation(value = "Create a new trip", notes = "Driver creates a new trip offer")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Trip created"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ApiResponseWrapper<TripResponse> createTrip(
        @Valid @RequestBody CreateTripRequest request,
        @RequestHeader(value = "X-User-Id", required = true) String userId
    ) {
        TripResponse response = tripService.createTrip(request);
        return ApiResponseWrapper.success(response);
    }

    @GetMapping("/{tripId}")
    @ApiOperation(value = "Get trip by ID")
    public ApiResponseWrapper<TripResponse> getTrip(
        @PathVariable String tripId
    ) {
        return ApiResponseWrapper.success(tripService.getTrip(tripId));
    }

    @GetMapping("/search")
    @ApiOperation(value = "Search available trips")
    public ApiResponseWrapper<Page<TripResponse>> searchTrips(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponseWrapper.success(searchService.search(page, size));
    }
}
```

---

## 3. Method Length Guidelines

### 3.1 Ideal Method Size

| Method Type | Maximum Lines | Rationale |
|------------|-------------|------------|
| Public API | 30 | Should be readable in one view |
| Private helper | 20 | Single responsibility |
| Controller | 10 | Mostly delegation |

### 3.2 Rule of Thumb

If a method exceeds 30 lines, consider:
1. Extract private helper methods
2. Extract to separate service
3. Create service locator pattern

**Example - Before (40 lines):**
```java
public TripResponse createTrip(CreateTripRequest request) {
    // Validation - 10 lines
    if (request.getSource() == null) {
        throw new BadRequestException("Source is required");
    }
    if (request.getDestination() == null) {
        throw new BadRequestException("Destination is required");
    }
    if (request.getTotalSeats() <= 0) {
        throw new BadRequestException("Seats must be positive");
    }
    if (request.getPricePerSeat() <= 0) {
        throw new BadRequestException("Price must be positive");
    }
    
    // Build entity - 10 lines
    Trip trip = Trip.builder()
        .tripId(generateId())
        .driverId(request.getDriverId())
        .sourceAddress(request.getSource())
        .destinationAddress(request.getDestination())
        .totalSeats(request.getTotalSeats())
        .pricePerSeat(request.getPricePerSeat())
        .status(TripStatus.CREATED)
        .build();
    
    // Save and return - 5 lines
    Trip saved = repository.save(trip);
    return toResponse(saved);
}
```

**Example - After (refactored):**
```java
public TripResponse createTrip(CreateTripRequest request) {
    validateRequest(request);
    Trip trip = buildTripEntity(request);
    Trip saved = repository.save(trip);
    return toResponse(saved);
}

private void validateRequest(CreateTripRequest request) {
    requireNonNull(request.getSource(), "Source is required");
    requireNonNull(request.getDestination(), "Destination is required");
    requirePositive(request.getTotalSeats(), "Seats must be positive");
    requirePositive(request.getPricePerSeat(), "Price must be positive");
}

private Trip buildTripEntity(CreateTripRequest request) {
    return Trip.builder()
        .tripId(generateId())
        .driverId(request.getDriverId())
        .sourceAddress(request.getSource())
        .destinationAddress(request.getDestination())
        .totalSeats(request.getTotalSeats())
        .pricePerSeat(request.getPricePerSeat())
        .status(TripStatus.CREATED)
        .build();
}

private void requireNonNull(Object value, String message) {
    if (value == null) {
        throw new BadRequestException(message);
    }
}

private void requirePositive(int value, String message) {
    if (value <= 0) {
        throw new BadRequestException(message);
    }
}
```

---

## 4. Error Handling

### 4.1 Exception Hierarchy

```
RuntimeException
├── BusinessException (common)
│   ├── ResourceNotFoundException
│   ├── UnauthorizedException
│   ├── ForbiddenException
│   └── BadRequestException
└── ServiceException (custom)
    ├── ServiceUnavailableException
    └── ExternalServiceException
```

### 4.2 Global Exception Handler Pattern

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ResourceNotFoundException ex, ServerWebExchange exchange) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ApiError.builder()
            .status(HttpStatus.NOT_FOUND)
            .code("NOT_FOUND")
            .message(ex.getMessage())
            .error(ErrorBody.builder()
                .path(exchange.getRequest().getURI().getPath())
                .build())
            .build();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusiness(BusinessException ex, ServerWebExchange exchange) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ApiError.builder()
            .status(ex.getErrorCode().getHttpStatus())
            .code(ex.getErrorCode().getCode())
            .message(ex.getMessage())
            .error(ErrorBody.builder()
                .path(exchange.getRequest().getURI().getPath())
                .detail(ex.getErrorCode().getDetail())
                .build())
            .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error", ex);
        return ApiError.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .error(ErrorBody.builder()
                .path(exchange.getRequest().getURI().getPath())
                .build())
            .build();
    }
}
```

### 4.3 Do's and Don'ts

| Do | Don't |
|----|-------|
| Use typed exceptions | Throw generic `RuntimeException` |
| Include error codes | Use only error messages |
| Log at appropriate level | Log and throw same exception |
| Include request context | Expose internal stack traces |
| Return consistent error format | Return different formats per endpoint |

---

## 5. Logging Standards

### 5.1 Log Levels

| Level | Use Case |
|-------|---------|
| ERROR | Exceptions, failures requiring attention |
| WARN | Recoverable issues, deprecated usage |
| INFO | Important business events (login, create, update) |
| DEBUG | Variable states, flow entry |

### 5.2 Logging Patterns

```java
@Slf4j
public class TripService {

    // Entry logging for public methods
    public TripResponse createTrip(CreateTripRequest request) {
        log.info("Creating trip for driver: {}", request.getDriverId());
        
        try {
            validateRequest(request);
            Trip trip = buildTripEntity(request);
            Trip saved = repository.save(trip);
            
            log.info("Trip created successfully: {}", saved.getTripId());
            auditService.log(AuditAction.CREATE, saved.getTripId());
            
            return toResponse(saved);
            
        } catch (BusinessException e) {
            throw e; // Re-throw, handler will log
            
        } catch (Exception e) {
            log.error("Failed to create trip for driver: {}", request.getDriverId(), e);
            throw new ServiceException("Failed to create trip", e);
        }
    }

    // Parameter logging for complex methods
    public List<Trip> searchTrips(TripSearchCriteria criteria) {
        log.debug("Searching trips with criteria: {}", criteria);
        
        List<Trip> trips = repository.findByStatus(criteria.getStatus());
        
        log.debug("Found {} trips", trips.size());
        return trips;
    }
}
```

---

## 6. Documentation

### 6.1 JavaDoc Requirements

**Required for:**
- All public service methods
- All controller endpoints
- Exception classes
- DTOs with non-obvious constraints

**Pattern:**
```java
/**
 * Creates a new trip offer.
 *
 * Requires driver approval and validated vehicle.
 * Trip must have at least 1 available seat.
 *
 * @param request trip creation request (non-null)
 * @param driverId authenticated driver ID from header
 * @return created trip with generated tripId
 * @throws ResourceNotFoundException if driver not found
 * @throws BadRequestException if validation fails
 * @throws UnauthorizedException if driver not approved
 */
public TripResponse createTrip(CreateTripRequest request, String driverId) {
    // ...
}
```

### 6.2 OpenAPI Annotations

```java
@Operation(
    summary = "Create a new trip",
    description = "Allows an approved driver to create a new trip offer",
    tags = {"Trip Management"}
)
@ApiParam(
    name = "X-User-Id", 
    required = true, 
    example = "driver-123"
)
@PostMapping("/api/trips")
public ApiResponseWrapper<TripResponse> createTrip(
    @Valid @RequestBody @ApiParam(
        description = "Trip details",
        required = true,
        examples = {
            @ExampleObject(
                name = "Valid request",
                value = "{ \"source\": \"...\", \"destination\": \"...\", \"seats\": 3 }"
            )
        }
    ) CreateTripRequest request
) {
    // ...
}
```

---

## 7. Testability Guidelines

### 7.1 Constructor Injection (Required)

```java
// GOOD - Enables mocking
@Service
@RequiredArgsConstructor
public class TripService {
    private final TripsRepository repository;
    private final AuditService auditService;
}

// BAD - Field injection hard to test
@Service
public class TripService {
    @Autowired
    private TripsRepository repository;
}
```

### 7.2 Package-Private for Testing

```java
// Service - package private helper for testing
class TripService {
    
    // Package-private for test access
    Trip buildTripEntity(CreateTripRequest request) {
        // ...
    }
    
    // Keep private what shouldn't be tested directly
    private void validateRequest(CreateTripRequest request) {
        // ...
    }
}
```

### 7.3 Avoid Static Coupling

```java
// BAD - Hard to mock
@Service
public class TripService {
    private final JwtService jwtService = JwtService.getInstance();
}

// GOOD - Inject dependency
@Service
public class TripService {
    private final JwtService jwtService;
}
```

---

## 8. Code Formatting Summary

| Aspect | Standard |
|--------|----------|
| Indentation | 4 spaces |
| Line length | 120 max |
| Blank lines | 1 between methods, 2 between classes |
| Import order | java → javax → internal → third-party |
| Annotations | Before type, one per line |
| Braces | Same line for classes, new line for methods |

---

## 9. Quick Reference Card

### Method Order in Service Class

1. Dependencies (final fields, constructor)
2. Public API methods (business operations)
3. Public query methods (read operations)
4. Private helpers (validation, building)
5. Mappers (toResponse, toEntity)

### Controller Method Order

1. POST (create)
2. PUT/PATCH (update)
3. GET (read single)
4. GET (list/search)
5. DELETE (delete)

### Exception Handler Order

1. Specific exceptions first
2. Business exceptions
3. Generic exceptions last

---

*Last Updated: April 2026*