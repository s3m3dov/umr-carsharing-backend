# API Documentation Architecture

## Overview

The API documentation is structured using an **interface-based pattern** that separates Swagger annotations from controller logic. This avoids cluttering business code with documentation boilerplate while keeping examples visible in Swagger UI.

## Structure

Each service follows this layout in its `controller/` package:

```
controller/
  AuthApi.java            ← Interface: @Operation, @ApiResponses, @Tag
  AuthApiExamples.java    ← Constants: JSON example payloads
  AuthController.java     ← Implementation: business logic only
```

### Why This Structure?

1. **Name clash resolution**: Swagger's `@ApiResponse` annotation and the project's `ApiResponseWrapper` DTO would clash if both lived in the same file. The interface isolates Swagger annotations from controller code.
2. **Readability**: Each endpoint has ~10 lines of Swagger annotations vs ~4 lines of logic. Mixing them makes controllers unreadable.
3. **Single responsibility**: API contract (interface) is separate from implementation (controller).

## File Roles

### Api Interface (e.g. `AuthApi.java`)

Holds all OpenAPI annotations: `@Tag`, `@Operation`, `@ApiResponses`, `@ApiResponse`, `@Parameter`.

```java
@Tag(name = "Authentication Management", description = "...")
public interface AuthApi {

    @Operation(summary = "User login", description = "...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = AuthApiExamples.LOGIN_RESPONSE))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    ResponseEntity<ApiResponseWrapper<LoginResponse>> login(LoginRequest request);
}
```

### ApiExamples (e.g. `AuthApiExamples.java`)

`final` utility class with `static final String` constants — JSON payloads for `@ExampleObject`.

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

### Controller (e.g. `AuthController.java`)

Implements the Api interface. Contains only Spring MVC annotations and business logic.

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
```

## Files Per Service

| Service | Interface | Examples | Controller |
|---------|-----------|----------|------------|
| Auth | `AuthApi.java` | `AuthApiExamples.java` | `AuthController.java` |
| User | `UserApi.java` | `UserApiExamples.java` | `UserController.java` |
| Vehicle | `VehicleApi.java` | `VehicleApiExamples.java` | `VehicleController.java` |
| Trip | `TripApi.java` | `TripApiExamples.java` | `TripController.java` |
| Booking | `BookingApi.java` | `BookingApiExamples.java` | `BookingController.java` |

All files live in each service's `controller/` package. No cross-package imports needed for examples since interface and examples are co-located.

## DTO Response Wrapper

The project uses `ApiResponseWrapper<T>` (in `com.cimhub.common.dto`) as the unified response envelope — not `ApiResponse`, which would clash with the Swagger annotation of the same name.

```java
// No clash — different names
import io.swagger.v3.oas.annotations.responses.ApiResponse;  // in Api interfaces
import com.cimhub.common.dto.ApiResponseWrapper;              // in controllers
```

## DTO-Level Examples

Request/response DTOs also reference examples via `@Schema`:

```java
@Schema(name = "LoginRequest", description = "User login credentials",
        example = AuthApiExamples.LOGIN_REQUEST)
public record LoginRequest(...) {}
```

These appear in Swagger UI schema sections and are defined in the same `*ApiExamples` class.

## Maintenance

### Update an example
Edit the constant in the `*ApiExamples.java` file. Changes propagate to Swagger UI on restart.

### Add a new endpoint
1. Add method signature + Swagger annotations to the Api interface
2. Add example constants to the ApiExamples class
3. Implement the method in the controller with `@Override`

### Add a new service
Follow the same 3-file pattern: `ServiceApi.java`, `ServiceApiExamples.java`, `ServiceController.java`.
