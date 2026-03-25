# Documentation Approach: Clean Code + Comprehensive Docs

## Problem Statement

Embedding extensive Swagger annotations and JSON examples directly in controllers:
- Makes source files bloated and hard to read (~10 lines of annotations per ~4 lines of logic)
- Mixes concerns (implementation logic vs. API documentation)
- Creates name clashes between `@ApiResponse` (Swagger) and `ApiResponseWrapper` (DTO)

## Solution: Interface-Based API Documentation

This project separates documentation from implementation using a **3-file pattern per service**:

```
controller/
  AuthApi.java            ← Interface with all Swagger annotations
  AuthApiExamples.java    ← JSON example payloads as constants
  AuthController.java     ← Clean implementation (business logic only)
```

### How It Works

**Api Interface** — defines the API contract with full OpenAPI annotations:
```java
@Tag(name = "Authentication Management", description = "...")
public interface AuthApi {

    @Operation(summary = "User login", description = "...")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.LOGIN_RESPONSE))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    ResponseEntity<ApiResponseWrapper<LoginResponse>> login(LoginRequest request);
}
```

**Controller** — implements the interface, contains only logic:
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
```

**Examples** — JSON payloads referenced by the interface:
```java
public final class AuthApiExamples {
    public static final String LOGIN_RESPONSE = """
            {
              "success": true,
              "data": { "token": "eyJhbGci...", "email": "user@example.com", "role": "DRIVER" },
              "message": "Login successful"
            }""";
}
```

### Benefits

| Aspect | Benefit |
|--------|---------|
| **Code readability** | Controllers contain only business logic |
| **Name clash** | `@ApiResponse` (Swagger) and `ApiResponseWrapper` (DTO) live in separate files |
| **Maintainability** | Update docs in interface, update examples in constants — no controller changes |
| **Swagger UI** | Full examples visible via `@ExampleObject` + `@Schema` on DTOs |
| **Discoverability** | API contract reviewable in one interface per service |

## Where Examples Live

| Type | Location | Visible In |
|------|----------|------------|
| **Endpoint response examples** | `*ApiExamples.java` constants → `@ExampleObject` in Api interface | Swagger UI |
| **DTO field examples** | `@Schema` on record fields | Swagger UI schema section |
| **Full request/response docs** | `.docs/technical/api-reference.md` | GitHub / IDE |
| **cURL + code samples** | `.docs/technical/integration-examples.md` | GitHub / IDE |
| **Schema definitions** | `.docs/technical/openapi-schemas.md` | GitHub / IDE |

## For Developers

### Adding a New Endpoint

1. Add method + Swagger annotations to the Api interface
2. Add JSON constants to the ApiExamples class
3. Implement with `@Override` in the controller
4. Add `@Schema` examples to any new DTOs
5. Update markdown docs in `.docs/technical/`

### Updating an Example

Edit the constant in `*ApiExamples.java`. Restart to see changes in Swagger UI.

### See Also

- **[Examples Import Approach](examples-import-approach.md)** — detailed file structure and naming conventions
- **[Examples Location Guide](examples-location-guide.md)** — where to find specific payloads
- **[API Reference](api-reference.md)** — complete endpoint documentation
