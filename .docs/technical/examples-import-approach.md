# Examples Import Approach

## Overview

All OpenAPI payload examples are now **imported from centralized files** instead of being embedded inline in code annotations. This provides:

✅ **Clean Code** - Annotations reference imported constants
✅ **Single Source of Truth** - Examples in one place per service
✅ **Easy Maintenance** - Update examples without touching annotations
✅ **Visible in Swagger** - Examples automatically show in OpenAPI/Swagger UI
✅ **Better Organization** - All examples organized by service

## Structure

### Example Files Location

Each service has an `ApiExamples.java` file:

```
auth-service/src/main/java/com/cimhub/authservice/config/
  └── ApiExamples.java          ← All auth endpoint examples

user-service/src/main/java/com/cimhub/userservice/config/
  └── ApiExamples.java          ← All user endpoint examples

vehicle-service/src/main/java/com/cimhub/vehicleservice/config/
  └── ApiExamples.java          ← All vehicle endpoint examples

trip-service/src/main/java/com/cimhub/tripservice/config/
  └── ApiExamples.java          ← All trip endpoint examples
```

### Example File Structure

Each `ApiExamples.java` contains static String constants:

```java
package com.cimhub.authservice.config;

public class ApiExamples {
    public static final String LOGIN_RESPONSE = """
            {
              "success": true,
              "data": {
                "token": "eyJhbGc...",
                "email": "user@example.com",
                "role": "DRIVER"
              },
              "message": "Login successful"
            }""";

    public static final String SIGNUP_RESPONSE = """...""";

    public static final String VALIDATE_TOKEN_RESPONSE = """...""";

    public static final String ERROR_INVALID_CREDENTIALS = """...""";
}
```

## How It Works

### Step 1: Define Example in ApiExamples.java
```java
public static final String LOGIN_RESPONSE = """
        {
          "success": true,
          "data": {
            "token": "eyJhbGc...",
            "email": "john.driver@example.com",
            "role": "DRIVER"
          },
          "message": "Login successful"
        }""";
```

### Step 2: Import in Controller
```java
import com.cimhub.authservice.config.ApiExamples;
```

### Step 3: Reference in @ApiResponse
```java
@ApiResponse(
    responseCode = "200",
    description = "Login successful",
    content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = ApiExamples.LOGIN_RESPONSE)
    )
)
public ResponseEntity<ApiResponse<LoginResponse>> login(...) { ... }
```

### Result
Examples automatically appear in:
- ✅ Swagger UI (http://localhost:PORT/service/swagger-ui.html)
- ✅ OpenAPI JSON spec (http://localhost:PORT/service/v3/api-docs)
- ✅ IDE hints and documentation

## Example Files Content

### Auth Service ApiExamples.java

**Success Responses:**
- `LOGIN_RESPONSE` - Successful login with token
- `SIGNUP_RESPONSE` - Successful registration with token
- `VALIDATE_TOKEN_RESPONSE` - Token validation success

**Error Responses:**
- `ERROR_INVALID_CREDENTIALS` - Login failed
- `ERROR_USER_EXISTS` - Registration: email already exists
- `ERROR_VALIDATION` - Input validation error
- `ERROR_INVALID_TOKEN` - Token validation failed

### User Service ApiExamples.java

**Success Responses:**
- `GET_USER_RESPONSE` - User profile retrieval
- `CREATE_PROFILE_RESPONSE` - Profile creation success

**Error Responses:**
- `ERROR_USER_NOT_FOUND` - User doesn't exist
- `ERROR_INVALID_DATA` - Invalid input

### Vehicle Service ApiExamples.java

**Success Responses:**
- `GET_VEHICLES_RESPONSE` - List of user's vehicles
- `REGISTER_VEHICLE_RESPONSE` - Vehicle registration success

**Error Responses:**
- `ERROR_VEHICLE_EXISTS` - Vehicle already registered
- `ERROR_VEHICLE_LIMIT` - Too many vehicles
- `ERROR_NO_VEHICLES` - No vehicles found

### Trip Service ApiExamples.java

**Success Responses:**
- `OFFER_TRIP_RESPONSE` - Trip creation success
- `SEARCH_TRIPS_RESPONSE` - Search results (pickup/destination)
- `ACTIVE_TRIPS_RESPONSE` - Active trips list
- `CANCEL_TRIP_RESPONSE` - Trip cancellation success

**Error Responses:**
- `ERROR_TRIP_NOT_FOUND` - Trip doesn't exist
- `ERROR_TRIP_IN_PROGRESS` - Can't cancel active trip

## How Payloads Show in Swagger

### Before (Inline - Cluttered)
```java
@ApiResponse(
    responseCode = "200",
    content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = """
        {
          "success": true,
          "data": {
            "token": "eyJhbGc...",
            "email": "user@example.com",
            "role": "DRIVER"
          }
        }""")
    )
)
```

### After (Imported - Clean)
```java
@ApiResponse(
    responseCode = "200",
    content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = ApiExamples.LOGIN_RESPONSE)
    )
)
```

**Both produce identical Swagger UI output!** ✨

## Swagger UI Screenshots

When you visit `http://localhost:8081/auth-service/swagger-ui.html`:

```
POST /api/auth/login

Responses:
  200 - Login successful
    {
      "success": true,
      "data": {
        "token": "eyJhbGc...",
        "email": "john.driver@example.com",
        "role": "DRIVER"
      },
      "message": "Login successful"
    }
```

The example comes from `ApiExamples.LOGIN_RESPONSE` constant.

## Maintenance Workflow

### Update an Example

1. **Find the constant** in service's `ApiExamples.java`
2. **Update the JSON** in the String constant
3. **Restart service** to reload specs
4. **Verify in Swagger UI** at `http://localhost:PORT/service/swagger-ui.html`

### Example: Update Login Response

**Before:**
```java
public static final String LOGIN_RESPONSE = """
        {
          "success": true,
          "data": {
            "token": "...",
            "email": "user@example.com",
            "role": "DRIVER"
          },
          "message": "Login successful"
        }""";
```

**After (if adding new field):**
```java
public static final String LOGIN_RESPONSE = """
        {
          "success": true,
          "data": {
            "token": "...",
            "email": "user@example.com",
            "role": "DRIVER",
            "expiresIn": 3600
          },
          "message": "Login successful"
        }""";
```

**That's it!** The update is immediately available in Swagger UI.

### Add a New Example

1. **Add constant to ApiExamples.java:**
```java
public static final String NEW_RESPONSE = """
        { ... }""";
```

2. **Reference in controller:**
```java
@ApiResponse(
    responseCode = "201",
    content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = ApiExamples.NEW_RESPONSE)
    )
)
```

## Where Payloads Are Visible

| Location | Payload Visible | How |
|----------|-----------------|-----|
| **Swagger UI** | ✅ Yes | Auto-generated from @Schema and @ApiResponse |
| **OpenAPI JSON** | ✅ Yes | `/v3/api-docs` endpoint |
| **Markdown Docs** | ✅ Yes | API_DOCUMENTATION.md has full examples |
| **Code** | ✅ Yes | Import and reference constants |
| **IDE** | ✅ Yes | Hover over constant shows value |

## Advantages Over Inline Examples

### Code Clarity
- Controllers focus on logic, not documentation
- Annotations stay readable and scannable
- Examples are separate, not embedded

### Maintainability
- Update example in one place (ApiExamples.java)
- Changes automatically reflect in Swagger
- No duplication across annotations

### Organization
- All examples for a service in one file
- Easy to find all responses for a service
- Related examples grouped together

### Reusability
- Use same example for multiple endpoints if applicable
- Avoid copy-paste duplication
- Consistent examples across related endpoints

## Service Files Created

### Auth Service
- **File:** `auth-service/src/main/java/com/cimhub/authservice/config/ApiExamples.java`
- **Examples:** Login, Signup, Token validation, Errors
- **Used in:** AuthController

### User Service
- **File:** `user-service/src/main/java/com/cimhub/userservice/config/ApiExamples.java`
- **Examples:** Get user, Create profile, Errors
- **Used in:** UserController

### Vehicle Service
- **File:** `vehicle-service/src/main/java/com/cimhub/vehicleservice/config/ApiExamples.java`
- **Examples:** Get vehicles, Register vehicle, Errors
- **Used in:** VehicleController

### Trip Service
- **File:** `trip-service/src/main/java/com/cimhub/tripservice/config/ApiExamples.java`
- **Examples:** Offer trip, Search, Cancel, History, Errors
- **Used in:** TripController

## Verification

### In Swagger UI
1. Start service: `mvn spring-boot:run`
2. Visit: `http://localhost:PORT/service/swagger-ui.html`
3. Click an endpoint
4. Expand "Responses" → "200"
5. See the payload example from ApiExamples constant

### In OpenAPI JSON
```bash
curl http://localhost:PORT/service/v3/api-docs | jq '.paths["/api/auth/login"].post.responses."200".content."application/json".examples'
```

### In Code
- Hover over constant name: IDE shows the JSON
- Go to definition: Opens ApiExamples.java
- Search: Find all usages of an example

## Summary

**Payload examples are:**
- ✅ Imported from centralized files (ApiExamples.java)
- ✅ Visible in Swagger UI
- ✅ Visible in OpenAPI JSON
- ✅ Easy to update in one place
- ✅ Organized by service
- ✅ No duplication across files
- ✅ Clean, readable code

**Best of both worlds:** Clean code + comprehensive examples visible in Swagger!
