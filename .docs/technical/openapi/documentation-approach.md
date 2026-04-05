# Documentation Approach: Clean Code + Comprehensive Docs

## Problem Statement

Embedding extensive Swagger annotations and JSON examples directly in controllers:
- Makes source files bloated and hard to read (~10 lines of annotations per ~4 lines of logic)
- Mixes concerns (implementation logic vs. API documentation)
- Creates name clashes between `@ApiResponse` (Swagger) and `ApiResponse` (DTO)

## Solution: Controller-Based Documentation with Separate Examples

This project uses a **2-file pattern per service**:

```
controller/
  AuthApiExamples.java    ← JSON example payloads as constants
  AuthController.java     ← Swagger annotations + business logic
```

### How It Works

**Controller** — contains Swagger annotations and business logic in one file:
```java
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "...")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User login", description = "...")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.LOGIN_RESPONSE))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
```

**Examples** — JSON payloads referenced by the controller:
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
| **Code readability** | Examples stored as constants, not embedded in annotations |
| **Name clash** | `@ApiResponse` (Swagger) and `ApiResponseWrapper` (DTO) — different names, no conflict |
| **Maintainability** | Update examples in `*ApiExamples.java` without touching controller logic |
| **Swagger UI** | Full examples visible via `@ExampleObject` + `@Schema` on DTOs |
| **Single file** | All endpoint docs live with their implementation |

## Where Examples Live

| Type | Location | Visible In |
|------|----------|------------|
| **Endpoint response examples** | `*ApiExamples.java` constants → `@ExampleObject` in controller | Swagger UI |
| **DTO field examples** | `@Schema` on record fields | Swagger UI schema section |
| **Full request/response docs** | `.docs/technical/api-reference.md` | GitHub / IDE |
| **cURL + code samples** | `.docs/technical/integration-examples.md` | GitHub / IDE |
| **Schema definitions** | `.docs/technical/openapi-schemas.md` | GitHub / IDE |

## For Developers

### Adding a New Endpoint

1. Add method + Swagger annotations to the controller
2. Add JSON constants to the `*ApiExamples.java` class
3. Implement the business logic in the controller method
4. Add `@Schema` examples to any new DTOs
5. Update markdown docs in `.docs/technical/`

### Updating an Example

Edit the constant in `*ApiExamples.java`. Restart to see changes in Swagger UI.

### See Also

- **[Examples Import Approach](examples-import-approach.md)** — detailed file structure and naming conventions
- **[Examples Location Guide](examples-location-guide.md)** — where to find specific payloads
- **[API Reference](../_main/api-reference.md)** — complete endpoint documentation