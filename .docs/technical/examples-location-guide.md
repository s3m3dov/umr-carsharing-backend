# Where to Find Payload Examples

## Quick Access Map

| What You Need | Where to Find It |
|---------------|-----------------|
| **Endpoint response examples** | `*ApiExamples.java` in each service's `controller/` package |
| **DTO field examples** | `@Schema` annotations on request/response records |
| **Full API docs with payloads** | `.docs/technical/api-reference.md` |
| **cURL / JS / Python / Java** | `.docs/technical/integration-examples.md` |
| **Schema definitions** | `.docs/technical/openapi-schemas.md` |
| **Interactive testing** | `http://localhost:PORT/service/swagger-ui.html` |

---

## Example Files by Service

All example files are in the `controller/` package alongside their Api interface.

### Auth Service
**File:** `auth-service/.../controller/AuthApiExamples.java`

| Constant | Description |
|----------|-------------|
| `LOGIN_REQUEST` | Login credentials |
| `SIGNUP_REQUEST` | Registration payload |
| `VALIDATE_TOKEN_REQUEST` | Token validation |
| `LOGIN_RESPONSE` | Login success with JWT |
| `SIGNUP_RESPONSE` | Registration success with JWT |
| `VALIDATE_TOKEN_RESPONSE` | Token validation result |
| `ERROR_INVALID_CREDENTIALS` | Bad login |
| `ERROR_USER_EXISTS` | Duplicate email |
| `ERROR_VALIDATION` | Input validation |
| `ERROR_INVALID_TOKEN` | Expired/invalid token |

### User Service
**File:** `user-service/.../controller/UserApiExamples.java`

| Constant | Description |
|----------|-------------|
| `CREATE_PROFILE_REQUEST` | Profile creation payload |
| `GET_USER_RESPONSE` | User profile |
| `CREATE_PROFILE_RESPONSE` | Profile created |
| `ERROR_USER_NOT_FOUND` | User doesn't exist |
| `ERROR_INVALID_DATA` | Invalid input |

### Vehicle Service
**File:** `vehicle-service/.../controller/VehicleApiExamples.java`

| Constant | Description |
|----------|-------------|
| `REGISTER_VEHICLE_REQUEST` | Vehicle registration |
| `GET_VEHICLES_RESPONSE` | List of vehicles |
| `REGISTER_VEHICLE_RESPONSE` | Registration success |
| `ERROR_VEHICLE_EXISTS` | Duplicate plate |
| `ERROR_VEHICLE_LIMIT` | Max 5 vehicles |
| `ERROR_NO_VEHICLES` | No vehicles found |

### Trip Service
**File:** `trip-service/.../controller/TripApiExamples.java`

| Constant | Description |
|----------|-------------|
| `OFFER_TRIP_REQUEST` | Create trip payload |
| `CANCEL_TRIP_REQUEST` | Cancel trip payload |
| `OFFER_TRIP_RESPONSE` | Trip created |
| `SEARCH_TRIPS_RESPONSE` | Search results |
| `ACTIVE_TRIPS_RESPONSE` | Active trips list |
| `CANCEL_TRIP_RESPONSE` | Trip cancelled |
| `ERROR_TRIP_NOT_FOUND` | Trip doesn't exist |
| `ERROR_TRIP_IN_PROGRESS` | Can't cancel active trip |

### Booking (Trip Service)
**File:** `trip-service/.../controller/BookingApiExamples.java`

| Constant | Description |
|----------|-------------|
| `JOIN_TRIP_REQUEST` | Join trip as passenger |
| `CANCEL_BOOKING_REQUEST` | Cancel booking |
| `JOIN_TRIP_RESPONSE` | Booking confirmed |
| `CANCEL_BOOKING_RESPONSE` | Booking cancelled |
| `ACTIVE_BOOKINGS_RESPONSE` | Active bookings list |
| `ERROR_TRIP_FULL` | No available seats |

---

## DTO-Level Examples

Request/response DTOs use `@Schema(example = ...)` for field-level examples visible in Swagger:

```
auth-service/.../dto/request/
  LoginRequest.java            ← @Schema(example = AuthApiExamples.LOGIN_REQUEST)
  RegisterUserRequest.java     ← @Schema(example = AuthApiExamples.SIGNUP_REQUEST)
  ValidateTokenRequest.java    ← @Schema(example = AuthApiExamples.VALIDATE_TOKEN_REQUEST)

auth-service/.../dto/response/
  LoginResponse.java           ← @Schema with inline field examples
  SignUpResponse.java           ← @Schema with inline field examples
  ValidateTokenResponse.java   ← @Schema with inline field examples
```

Same pattern for user-service, vehicle-service, and trip-service DTOs.

---

## Swagger UI

| Service | URL |
|---------|-----|
| Auth Service | http://localhost:8081/auth-service/swagger-ui.html |
| User Service | http://localhost:8082/user-service/swagger-ui.html |
| Vehicle Service | http://localhost:8083/vehicle-service/swagger-ui.html |
| Trip Service | http://localhost:8084/trip-service/swagger-ui.html |

---

## See Also

- **[Examples Import Approach](examples-import-approach.md)** — architecture and naming conventions
- **[Documentation Approach](documentation-approach.md)** — philosophy behind the separation
