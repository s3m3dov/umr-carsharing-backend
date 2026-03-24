# Documentation Approach: Clean Code + Comprehensive Docs

## Problem Statement

Embedding extensive examples and documentation directly in code annotations can:
- Make source files bloated and hard to read
- Mix concerns (implementation logic vs. documentation)
- Create duplication when examples appear in multiple places
- Make IDE navigation and code comprehension harder

## Solution: Separation of Concerns

This project uses a **clean code + comprehensive documentation** approach:

### 1. Code Annotations (Minimal, Focused)

**Controllers:**
```java
@Operation(
    summary = "Brief action description",
    description = "Extended explanation of functionality. Reference markdown files for detailed examples."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success case"),
    @ApiResponse(responseCode = "404", description = "Error case"),
    @ApiResponse(responseCode = "409", description = "Conflict case")
})
public ResponseEntity<ApiResponse<Data>> methodName() { ... }
```

**DTOs:**
```java
@Schema(
    description = "What this object represents",
    example = """
        {
          "field1": "example_value",
          "field2": "another_value"
        }
        """
)
public record RequestDTO(
    @Schema(description = "Field purpose", example = "value")
    String field1,

    @Schema(description = "Another field", example = "value2")
    String field2
) { }
```

**Benefits:**
- Annotations remain readable and scannable
- Code files stay concise
- Examples visible in Swagger UI via `@Schema`
- Focuses on API surface, not implementation details

### 2. Markdown Documentation (Comprehensive, Detailed)

**Organization:**
```
.docs/
├── README.md                      ← Navigation guide
├── API_DOCUMENTATION.md           ← Complete API reference with detailed examples
├── API_INTEGRATION_EXAMPLES.md    ← Code examples in multiple languages
├── OPENAPI_SCHEMAS.md             ← Data model references
└── DOCUMENTATION_APPROACH.md      ← This file
```

**Content in Markdown:**
- Full request/response examples
- cURL commands
- Code samples (JavaScript, Python, Java)
- Step-by-step integration guides
- Error scenarios with examples
- Architecture diagrams
- Real-world workflows

**Benefits:**
- Single source of truth for examples
- Easy to update without touching code
- Human-readable navigation
- Searchable text format
- Can be versioned independently

## The Approach in Practice

### Before (Verbose)
```java
@Operation(summary = "Login")
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Login successful",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                  "success": true,
                  "data": {
                    "token": "eyJhbGci...",
                    "email": "user@example.com",
                    "role": "DRIVER"
                  }
                }
                """)
        )
    ),
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
})
public ResponseEntity<...> login(...) { ... }
```

### After (Clean)
```java
@Operation(
    summary = "User login",
    description = "Authenticates user with email/password. See API_DOCUMENTATION.md for examples."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
})
public ResponseEntity<...> login(...) { ... }
```

**In API_DOCUMENTATION.md:**
```markdown
#### 1.2 Login

**POST** `/api/auth/login`

Authenticates a user and returns a JWT token.

**Request:**
```json
{
  "email": "john.driver@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "john.driver@example.com",
    "role": "DRIVER"
  },
  "message": "Login successful"
}
```
...
```

## Where Examples Live

| Type | Location | Visibility |
|------|----------|-----------|
| **Concise DTO examples** | `@Schema` annotation | Swagger UI + IDE |
| **Request/Response examples** | API_DOCUMENTATION.md | GitHub, IDE, Web |
| **cURL commands** | API_INTEGRATION_EXAMPLES.md | GitHub, IDE, Web |
| **Code samples (JS/Python/Java)** | API_INTEGRATION_EXAMPLES.md | GitHub, IDE, Web |
| **Data model definitions** | OPENAPI_SCHEMAS.md | GitHub, IDE, Web |
| **Validation rules** | OPENAPI_SCHEMAS.md + code `@Schema` | GitHub + Swagger UI |
| **Error scenarios** | API_DOCUMENTATION.md | GitHub, IDE, Web |

## Accessing Documentation

### For API Consumers
1. **Start with**: [README.md](./) - Quick navigation
2. **Learn API**: [API_DOCUMENTATION.md](./api-reference.md) - Complete reference
3. **Code samples**: [API_INTEGRATION_EXAMPLES.md](./integration-examples.md) - Copy & adapt
4. **Schema details**: [OPENAPI_SCHEMAS.md](./openapi-schemas.md) - Validation rules
5. **Interactive**: Visit `http://localhost:PORT/service/swagger-ui.html` - Try endpoints

### For API Developers
1. **Read approach**: This file (DOCUMENTATION_APPROACH.md)
2. **Add endpoint**:
   - Add `@Operation` + `@ApiResponses` to controller
   - Add `@Schema` to DTOs with concise examples
   - Reference markdown files in descriptions
   - Update markdown files with detailed examples
3. **Verify in Swagger UI**: Visit service's swagger-ui.html

## Annotation Guidelines

### @Operation
- **Summary** (5-10 words): Action-oriented, concise
- **Description**: Explain "why" and "when", reference markdown for examples

```java
@Operation(
    summary = "Get user vehicles",
    description = "Retrieves active vehicles for a driver. See API_DOCUMENTATION.md for examples."
)
```

### @ApiResponse
- **responseCode**: HTTP status (200, 400, 404, etc.)
- **description**: What happens in this case (brief)
- **NO example JSON**: Point to markdown instead

```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved"),
    @ApiResponse(responseCode = "404", description = "No vehicles found")
})
```

### @Schema (DTOs)
- **description**: What field represents
- **example**: Valid example value
- **Include multiline examples** for complex objects

```java
@Schema(
    description = "User login credentials",
    example = """
        {
          "email": "user@example.com",
          "password": "SecurePassword123!"
        }
        """
)
public record LoginRequest(...) { }
```

### @Parameter
- **description**: What parameter does, constraints
- **example**: Valid example value

```java
@Parameter(
    description = "User's unique identifier",
    example = "user_123"
)
@PathVariable String userId
```

## Markdown File Structure

### API_DOCUMENTATION.md
- Service overview
- Authentication & response formats
- Detailed endpoint documentation
- Error codes and handling
- Real-world usage examples

### API_INTEGRATION_EXAMPLES.md
- cURL examples for all endpoints
- JavaScript/Node.js patterns
- Python client code
- Java client code
- Common patterns (error handling, pagination, rate limiting)

### OPENAPI_SCHEMAS.md
- Complete schema definitions (JSON)
- Validation rules and constraints
- Enum definitions
- Field size restrictions

## Maintenance

### When Code Changes
1. Update controller annotations (if signature changes)
2. Update DTO `@Schema` (if fields change)
3. Update corresponding markdown section
4. Test in Swagger UI: `http://localhost:PORT/service/swagger-ui.html`

### When Adding New Endpoint
1. Create controller method with `@Operation` + `@ApiResponses`
2. Create/update DTOs with `@Schema` examples
3. Add section to API_DOCUMENTATION.md
4. Add code examples to API_INTEGRATION_EXAMPLES.md
5. Add schemas to OPENAPI_SCHEMAS.md

### Updating Examples
- **Change applies everywhere?** Update markdown
- **Change is service-specific?** Update markdown for that service
- **Examples are stale?** Run them against API to verify
- **New language support?** Add to API_INTEGRATION_EXAMPLES.md

## Benefits of This Approach

| Aspect | Benefit |
|--------|---------|
| **Code Readability** | Controllers stay clean and scannable |
| **Documentation** | Examples in one place, easy to find and update |
| **Swagger UI** | Still shows concise examples from `@Schema` |
| **IDE Experience** | Fast navigation, clear method signatures |
| **Maintenance** | Update examples without touching code |
| **Discoverability** | Markdown files are searchable on GitHub |
| **Scalability** | Easy to add more language examples |
| **Separation** | Implementation separate from documentation |

## Quick Reference

### To Add a New Endpoint
```java
// 1. Controller with minimal annotations
@Operation(
    summary = "Brief description",
    description = "Extended description. See API_DOCUMENTATION.md for examples."
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Resource created"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
})
@PostMapping("/resource")
public ResponseEntity<ApiResponse<ResponseDTO>> createResource(
    @Valid @RequestBody RequestDTO request) {
    // implementation
}

// 2. DTOs with examples
@Schema(
    description = "Request to create resource",
    example = """
        {"field1": "value1", "field2": "value2"}
        """
)
public record RequestDTO(...) { }

// 3. Update markdown files
// - Add to API_DOCUMENTATION.md with full request/response
// - Add code examples to API_INTEGRATION_EXAMPLES.md
// - Add schemas to OPENAPI_SCHEMAS.md
```

## File Locations

**Code:**
- Controllers: `{service}/src/main/java/{package}/controller/`
- DTOs: `{service}/src/main/java/{package}/dto/`
- Config: `{service}/src/main/java/{package}/config/OpenApiConfig.java`

**Documentation:**
- All in `.docs/` directory
- Markdown files are source of truth for examples

## Summary

This approach provides:
- ✅ Clean, readable code
- ✅ Comprehensive, detailed documentation
- ✅ Single source of truth for examples
- ✅ Interactive Swagger UI
- ✅ Searchable markdown files
- ✅ Easy maintenance and updates
- ✅ Multi-language code examples
- ✅ Clear separation of concerns

**Next time you add a feature:** Check this file for the process!
