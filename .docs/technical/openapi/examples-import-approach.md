# API Documentation Architecture

## Overview

The API documentation uses **Swagger annotations directly on controllers** with examples stored in separate `*ApiExamples.java` files. This keeps JSON payloads out of annotations while maintaining clean, self-documented controllers.

## Structure

Each service follows this layout in its `controller/` package:

```
controller/
  AuthApiExamples.java    ← Constants: JSON example payloads
  AuthController.java     ← @Tag, @Operation, @ApiResponses + business logic
```

### Why This Structure?

1. **Clean annotations**: Examples stored as constants avoid embedding large JSON in `@ExampleObject`.
2. **Name clash resolution**: Swagger's `@ApiResponse` annotation and the project's `ApiResponseWrapper` DTO coexist cleanly because the DTO is renamed to avoid the clash.
3. **Maintainability**: Editing examples in a dedicated file is cleaner than scrolling through annotation-littered controllers.
4. **Single file per controller**: All endpoint documentation lives with the implementation for easier navigation.

## File Roles

### Controller (e.g. `AuthController.java`)

Holds Spring MVC annotations, Swagger annotations, and business logic. All in one file.

```java
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "User registration, login, and token validation APIs...")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password..."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = AuthApiExamples.LOGIN_RESPONSE))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
```

### ApiExamples (e.g. `AuthApiExamples.java`)

`final` utility class with `static final String` constants — JSON payloads referenced by Swagger `@ExampleObject` annotations.

```java
public final class AuthApiExamples {
    private AuthApiExamples() {}

    public static final String LOGIN_RESPONSE = """
            {
              "success": true,
              "data": {
                "token": "eyJhbGci...",
                "email": "john.driver@example.com",
                "role": "DRIVER"
              },
              "message": "Login successful"
            }""";
}
```

## Files Per Service

| Service | Examples | Controller |
|---------|----------|------------|
| Auth | `AuthApiExamples.java` | `AuthController.java` |
| User | `UserApiExamples.java` | `UserController.java` |
| Vehicle | `VehicleApiExamples.java` | `VehicleController.java` |
| Trip | `TripApiExamples.java` | `TripController.java` |
| Booking | `BookingApiExamples.java` | `BookingController.java` |
| Review | `ReviewApiExamples.java` | `ReviewController.java` |

All files live in each service's `controller/` package. No cross-package imports needed since both controller and examples are co-located.

For response schemas that include audit timestamps, DTOs document both `createdAt` and `updatedAt` as UTC datetimes (for example, `2026-03-30T12:10:30.123Z`).

## DTO Response Wrapper

The project uses `ApiResponseWrapper<T>` (in `com.cimhub.common.dto`) as the unified response envelope. The DTO was renamed from `ApiResponse` to avoid naming conflicts with Swagger's `@ApiResponse` annotation.

```java
import io.swagger.v3.oas.annotations.responses.ApiResponse;        // Swagger annotation
import com.cimhub.common.dto.response.ApiSuccess;                  // Response DTO
```

## DTO-Level Documentation

Request/response DTOs use `@Schema` annotations for descriptions and examples:

```java
@Schema(name = "LoginRequest", description = "User login credentials",
        example = AuthApiExamples.LOGIN_REQUEST)
public record LoginRequest(...) {}
```

Field-level descriptions are also supported:

```java
@Schema(description = "User's email address", example = "john.driver@example.com")
String email,
```

These appear in Swagger UI schema sections.

## Maintenance

### Update an example
Edit the constant in the `*ApiExamples.java` file. Changes propagate to Swagger UI on restart.

### Add a new endpoint
1. Add method signature + Swagger annotations (`@Operation`, `@ApiResponses`) to the controller
2. Add example constants to the `*ApiExamples.java` class
3. Implement the business logic in the controller method

### Add a new service
Follow the same 2-file pattern: `ServiceApiExamples.java`, `ServiceController.java`.
