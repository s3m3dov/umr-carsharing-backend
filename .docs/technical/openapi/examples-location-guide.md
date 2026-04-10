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

All example files are in the `controller/` package alongside their controller.

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
| `GET_USER_RESPONSE` | User profile |
| `ERROR_USER_NOT_FOUND` | User doesn't exist |

Role profile request DTOs:
- `user-service/.../dto/request/CreateDriverProfileRequest.java`
- `user-service/.../dto/request/UpdateDriverRequest.java`
- `user-service/.../dto/request/UpdatePassengerRequest.java`

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

### Review Service
**File:** `review-service/.../controller/ReviewApiExamples.java`

| Constant | Description |
|----------|-------------|
| `SUBMIT_REVIEW_REQUEST` | Submit review payload |
| `SUBMIT_DRIVER_REVIEW_RESPONSE` | Driver review created |
| `SUBMIT_PASSENGER_REVIEW_RESPONSE` | Passenger review created |
| `GET_DRIVER_REVIEWS_RESPONSE` | Driver reviews list |
| `GET_PASSENGER_REVIEWS_RESPONSE` | Passenger reviews list |
| `GET_AVERAGE_RATING_RESPONSE` | Average rating result |

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

Same pattern for user-service and trip-service DTOs.

Audit timestamp fields for changed response schemas:
- `trip-service/.../dto/response/AdminTripResponse.java` (`createdAt`, `updatedAt` in UTC)
- `user-service/.../dto/response/AdminVehicleResponse.java` (`createdAt`, `updatedAt` in UTC)
- `review-service/.../dto/response/ReviewResponse.java` (`createdAt`, `updatedAt` in UTC)

---

## Swagger UI

| Service | URL |
|---------|-----|
| Auth Service | http://localhost:8081/auth-service/swagger-ui.html |
| User Service | http://localhost:8082/user-service/swagger-ui.html |
| Trip Service | http://localhost:8084/trip-service/swagger-ui.html |
| Review Service | http://localhost:8085/review-service/swagger-ui.html |

---

## See Also

- **[Examples Import Approach](examples-import-approach.md)** — architecture and naming conventions
- **[Documentation Approach](documentation-approach.md)** — philosophy behind the separation
