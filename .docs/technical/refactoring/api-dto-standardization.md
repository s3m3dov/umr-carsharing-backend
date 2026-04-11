# API & DTO Standardization Guide

**Purpose:** Standardize API examples, DTO structures, and request/response patterns across all microservices  
**Scope:** All REST endpoints and data transfer objects  

---

## 1. Current State Analysis

### 1.1 Existing API Examples Files

| Service | File | Location |
|---------|------|----------|
| auth-service | AuthApiExamples.java | controller/ |
| user-service | UserApiExamples.java | controller/ |
| user-service | VehicleApiExamples.java | vehicle/controller/ |
| trip-service | TripApiExamples.java | controller/ |
| trip-service | BookingApiExamples.java | controller/ |
| review-service | ReviewApiExamples.java | controller/ |
| notification-service | NotificationApiExamples.java | controller/ |

### 1.2 Issues Identified

| Issue | Description | Impact |
|-------|-------------|-------|
| Inconsistent location | VehicleApiExamples is in vehicle/controller/ | Hard to find |
| No standard interface | Each service defines its own structure |Inconsistent usage |
| Not linked to OpenAPI | Examples not used in @Operation annotations | Duplicate effort |
| Static final everywhere | No organization by category | Hard to navigate |
| No error examples | Some services missing error examples | Incomplete docs |

---

## 2. Recommended File Structure

### 2.1 New Structure

```
src/main/java/com/cimhub/{service}/
├── controller/
│   ├── {Domain}Controller.java      # Main controller
│   └── {Domain}ApiDoc.java       # OpenAPI documentation constants
├── dto/
│   ├── request/
│   │   ├── CreateRequest.java
│   │   ├── UpdateRequest.java
│   │   └── PaginationRequest.java
│   ├── response/
│   │   ├── SuccessResponse.java
│   │   ├── ErrorResponse.java
│   │   └── paginated/
│   │       └── PageResponse.java
│   └── examples/
│       ├── RequestExamples.java
│       ├── ResponseExamples.java
│       └── ErrorExamples.java
```

### 2.2 Example: Standardized ApiDoc Class

```java
package com.cimhub.authservice.controller;

/**
 * OpenAPI documentation constants for Auth Service.
 * Contains example payloads for API documentation.
 */
public final class AuthApiDoc {

    private AuthApiDoc() {}

    // ========== REQUESTS ==========
    
    public static final class Requests {
        private Requests() {}
        
        public static final String LOGIN = """
            {
              "email": "john.driver@example.com",
              "password": "SecurePassword123!"
            }""";
        
        public static final String SIGNUP = """
            {
              "firstName": "John",
              "lastName": "Driver",
              "email": "john.driver@example.com",
              "password": "SecurePassword123!",
              "userType": "DRIVER",
              "phoneNumber": "+49 123 456789",
              "age": 32,
              "licenseNumber": "LD-123456789"
            }""";
        
        public static final String VALIDATE_TOKEN = """
            {
              "token": "eyJhbGciOiJIUzI1NiIs..."
            }""";
    }

    // ========== RESPONSES ==========
    
    public static final class Responses {
        private Responses() {}
        
        public static final String LOGIN_SUCCESS = """
            {
              "success": true,
              "data": {
                "token": "eyJhbGciOiJIUzI1NiIs...",
                "email": "john.driver@example.com",
                "role": "DRIVER"
              },
              "message": "Login successful"
            }""";
        
        public static final String SIGNUP_SUCCESS = """
            {
              "success": true,
              "data": {
                "token": "eyJhbGciOiJIUzI1NiIs...",
                "email": "john.driver@example.com",
                "role": "DRIVER"
              },
              "message": "User registered successfully"
            }""";
    }

    // ========== ERRORS ==========
    
    public static final class Errors {
        private Errors() {}
        
        public static final String INVALID_CREDENTIALS = """
            {
              "status": 401,
              "code": "INVALID_CREDENTIALS",
              "message": "Invalid email or password",
              "error": {
                "path": "/api/auth/login",
                "details": []
              },
              "timestamp": "2026-04-10T10:00:00.000Z",
              "requestId": "req_abc123"
            }""";
        
        public static final String USER_EXISTS = """
            {
              "status": 409,
              "code": "USER_ALREADY_EXISTS",
              "message": "User with email john.driver@example.com already exists",
              "error": {
                "path": "/api/auth/signup",
                "details": []
              },
              "timestamp": "2026-04-10T10:00:00.000Z",
              "requestId": "req_abc124"
            }""";
        
        public static final String VALIDATION = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid input data",
              "error": {
                "path": "/api/auth/signup",
                "details": [
                  { "field": "email", "message": "Email is required", "type": "NotBlank" },
                  { "field": "password", "message": "Password must be at least 8 characters", "type": "Size" }
                ]
              },
              "timestamp": "2026-04-10T10:00:00.000Z",
              "requestId": "req_abc125"
            }""";
    }
}
```

---

## 3. DTO Structure Standardization

### 3.1 Standard Request DTO

```java
package com.cimhub.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user login.
 */
public record LoginRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}
```

### 3.2 Standard Response DTO

```java
package com.cimhub.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for successful login.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
    String token,
    String email,
    String role
) {
    public static LoginResponse from(com.cimhub.authservice.model.User user, String token) {
        return new LoginResponse(token, user.getEmail(), user.getUserType().name());
    }
}
```

### 3.3 Standard Error Response

```java
package com.cimhub.common.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response structure.
 */
public record ApiError(
    int status,
    String code,
    String message,
    ErrorBody error,
    Instant timestamp,
    String requestId
) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(
            status,
            code,
            message,
            ErrorBody.builder()
                .path(path)
                .details(List.of())
                .build(),
            Instant.now(),
            null
        );
    }
}
```

---

## 4. Controller Integration

### 4.1 Using Examples in OpenAPI

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Api(tags = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user and return JWT token",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Success",
                        value = AuthApiDoc.Requests.LOGIN
                    )
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Invalid credentials",
                        value = AuthApiDoc.Errors.INVALID_CREDENTIALS
                    )
                )
            )
        }
    )
    public ApiResponseWrapper<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponseWrapper.success(authService.login(request));
    }
}
```

---

## 5. Package Organization

### 5.1 Current Problems

```
dto/
├── request/        # Mixed - some used, some unused
│   ├── LoginRequest.java
│   ├── RegisterUserRequest.java  # Not used - from auth integration
│   └── ...
├── response/      # Mixed - some duplicated across services
│   ├── LoginResponse.java
│   └── ...
└── (root)         # OSRM DTOs mixed with business DTOs
    ├── Geometry.java
    ├── TripResponse.java
    └── ...
```

### 5.2 Recommended Organization

```
dto/
├── request/
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   └── RegisterUserRequest.java
│   ├── trip/
│   │   ├── OfferRideRequest.java
│   │   └── JoinTripRequest.java
│   └── common/
│       ├── PaginationRequest.java
│       └── FilterRequest.java
├── response/
│   ├── auth/
│   │   ├── LoginResponse.java
│   │   └── SignUpResponse.java
│   ├── trip/
│   │   ├── TripResponse.java
│   │   └── BookingResponse.java
│   ├── common/
│   │   └── ApiResponseWrapper.java
│   └── error/
│       ├── ApiError.java
│       └── ErrorDetail.java
├── osrm/                    # External API DTOs
│   ├── OsrmResponse.java
│   ├── Geometry.java
│   └── Waypoint.java
└── examples/                # API documentation
    ├── AuthApiDoc.java
    ├── TripApiDoc.java
    └── ErrorExamples.java
```

---

## 6. Migration Steps

### Step 1: Create ApiDoc Classes (Per Service)

For each service, convert existing `*ApiExamples.java` to `*ApiDoc.java` with nested static classes:

```java
// Before
public final class AuthApiExamples {
    public static final String LOGIN_REQUEST = "...";
    public static final String LOGIN_RESPONSE = "...";
}

// After
public final class AuthApiDoc {
    public static final class Requests {
        public static final String LOGIN = "...";
    }
    public static final class Responses {
        public static final String LOGIN = "...";
    }
}
```

### Step 2: Move OSRM DTOs to Sub-Package

```java
// Before
trip-service/dto/OsrmResponse.java

// After  
trip-service/dto/osrm/OsrmResponse.java
```

### Step 3: Add Error Examples to All Services

Each service should have error examples for:
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 500 Internal Server Error

### Step 4: Link to OpenAPI Annotations

Update controllers to use examples:

```java
@PostMapping
@Operation(
    summary = "Create trip",
    responses = {
        @ApiResponse(
            responseCode = "201",
            content = @Content(
                examples = @ExampleObject(
                    value = TripApiDoc.Responses.CREATE_SUCCESS
                )
            )
        )
    }
)
public ApiResponseWrapper<TripResponse> createTrip(...) {}
```

---

## 7. Quick Reference

### 7.1 Response Wrapper Standard

```java
{
  "success": true,
  "data": { ... },        // or primitive, or array, or null
  "message": "Optional success message"
}
```

### 7.2 Error Response Standard

```java
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Human readable message",
  "error": {
    "path": "/api/trips",
    "details": [
      { "field": "email", "message": "Invalid format", "type": "Email" }
    ]
  },
  "timestamp": "2026-04-10T10:00:00.000Z",
  "requestId": "req_abc123"
}
```

### 7.3 Status Codes by Scenario

| Status | Use Case |
|--------|----------|
| 200 | Successful GET, PUT, PATCH |
| 201 | Successful POST (create) |
| 204 | Successful DELETE (no content) |
| 400 | Validation error, bad request |
| 401 | Authentication required |
| 403 | Permission denied |
| 404 | Resource not found |
| 409 | Conflict (duplicate, invalid state) |
| 422 | Business rule violation |
| 500 | Unexpected server error |

---

## 8. Common Error Codes

| Code | HTTP Status | Description |
|------|------------|-------------|
| VALIDATION_ERROR | 400 | Request validation failed |
| INVALID_CREDENTIALS | 401 | Wrong email/password |
| INVALID_TOKEN | 401 | Expired or invalid JWT |
| UNAUTHORIZED | 401 | Authentication required |
| FORBIDDEN | 403 | No permission |
| NOT_FOUND | 404 | Resource not found |
| ALREADY_EXISTS | 409 | Duplicate resource |
| INVALID_STATE | 409 | Invalid state transition |
| BUSINESS_RULE_VIOLATION | 422 | Business rule violated |
| INTERNAL_ERROR | 500 | Unexpected error |

---

*Last Updated: April 2026*