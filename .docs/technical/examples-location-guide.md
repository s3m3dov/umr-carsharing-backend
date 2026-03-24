# Where to Find Payload Examples

This guide shows you exactly where all the request/response payload examples are located and how to access them.

## Quick Access Map

| What You Need | Where to Find It | Format |
|---------------|-----------------|--------|
| **Login Request Payload** | API_DOCUMENTATION.md § 1.2 | JSON |
| **Register Request Payload** | API_DOCUMENTATION.md § 1.1 | JSON |
| **Response Examples** | API_DOCUMENTATION.md (each endpoint) | JSON |
| **Complete cURL Commands** | API_INTEGRATION_EXAMPLES.md § cURL Examples | bash |
| **JavaScript/Node.js Examples** | API_INTEGRATION_EXAMPLES.md § JavaScript | javascript |
| **Python Examples** | API_INTEGRATION_EXAMPLES.md § Python | python |
| **Java Examples** | API_INTEGRATION_EXAMPLES.md § Java | java |
| **Data Schemas & Validation** | OPENAPI_SCHEMAS.md | JSON Schema |
| **Interactive Testing** | http://localhost:PORT/service/swagger-ui.html | Swagger UI |

---

## Payload Examples by Service

### Auth Service

#### Login Request
**Location:** [API_DOCUMENTATION.md § 1.2](./api-reference.md#12-login)

```json
{
  "email": "john.driver@example.com",
  "password": "SecurePassword123!"
}
```

#### Register Request
**Location:** [API_DOCUMENTATION.md § 1.1](./api-reference.md#11-register-user-sign-up)

```json
{
  "firstName": "John",
  "lastName": "Driver",
  "email": "john.driver@example.com",
  "password": "SecurePassword123!",
  "userType": "DRIVER",
  "phoneNumber": "+49 123 456789",
  "age": 32,
  "vehicleModel": "Tesla Model 3",
  "licensePlate": "HE-XX-1234"
}
```

#### Login Response
**Location:** [API_DOCUMENTATION.md § 1.2](./api-reference.md#12-login)

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

#### Validate Token Request
**Location:** [API_DOCUMENTATION.md § 1.3](./api-reference.md#13-validate-token)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Location in Code:** See `@Schema` example in:
- [`auth-service/src/main/java/.../dto/request/ValidateTokenRequest.java`](../auth-service/src/main/java/com/cimhub/authservice/dto/request/ValidateTokenRequest.java)

---

### User Service

#### Get User Response
**Location:** [API_DOCUMENTATION.md § 2.1](./api-reference.md#21-get-user-information)

```json
{
  "success": true,
  "data": {
    "userId": "user_123",
    "firstName": "John",
    "lastName": "Driver",
    "email": "john.driver@example.com",
    "phoneNumber": "+49 123 456789",
    "age": 32,
    "role": "DRIVER",
    "profileCompletedAt": "2024-01-15T10:30:00Z"
  },
  "message": "User profile retrieved successfully"
}
```

#### Create Profile Request
**Location:** [API_DOCUMENTATION.md § 2.2](./api-reference.md#22-create-user-profile)

```json
{
  "userId": "user_123",
  "firstName": "John",
  "lastName": "Driver",
  "age": 32,
  "phoneNumber": "+49 123 456789"
}
```

**Location in Code:** See `@Schema` example in:
- [`user-service/src/main/java/.../dto/request/CreateProfileRequest.java`](../user-service/src/main/java/com/cimhub/userservice/dto/request/CreateProfileRequest.java)

---

### Vehicle Service

#### Register Vehicle Request
**Location:** [API_DOCUMENTATION.md § 3.2](./api-reference.md#32-register-vehicle)

```json
{
  "userId": "user_123",
  "vehicleName": "Tesla Model 3",
  "vehicleNumber": "HE-XX-1234",
  "vehicleType": "sedan",
  "vehicleColor": "white",
  "seatingCapacity": "5"
}
```

**Location in Code:** See `@Schema` example in:
- [`vehicle-service/src/main/java/.../dto/request/RegisterVehicleRequest.java`](../vehicle-service/src/main/java/com/cimhub/vehicleservice/dto/request/RegisterVehicleRequest.java)

#### Get Vehicles Response
**Location:** [API_DOCUMENTATION.md § 3.1](./api-reference.md#31-get-user-vehicles)

```json
{
  "success": true,
  "data": [
    {
      "value": "vehicle_001",
      "text": "Tesla Model 3 - HE-XX-1234",
      "seatingCapacity": "5"
    }
  ],
  "message": "Vehicles retrieved successfully"
}
```

**Location in Code:** See `@Schema` example in:
- [`vehicle-service/src/main/java/.../dto/response/VehicleResponse.java`](../vehicle-service/src/main/java/com/cimhub/vehicleservice/dto/response/VehicleResponse.java)

---

### Trip Service

#### Offer Trip Request
**Location:** [API_DOCUMENTATION.md § 4.1](./api-reference.md#41-offer-a-trip-driver-creates-a-ride)

```json
{
  "driverId": "user_123",
  "vehicleId": "vehicle_001",
  "sourceLatitude": 50.8154,
  "sourceLongitude": 8.6747,
  "destinationLatitude": 50.7265,
  "destinationLongitude": 7.0895,
  "departureTime": "2024-03-25T08:00:00Z",
  "availableSeats": 3,
  "pricePerSeat": 15.50,
  "notes": "Morning commute to Frankfurt"
}
```

**Location in Code:** See `@Schema` example in:
- [`trip-service/src/main/java/.../dto/request/OfferRideRequest.java`](../trip-service/src/main/java/com/cimhub/tripservice/dto/request/OfferRideRequest.java)

#### Search Matching Trips Query
**Location:** [API_DOCUMENTATION.md § 4.5](./api-reference.md#45-search-trips-matching-route)

```
GET /api/trips/search/matching-route?sourceLatitude=50.8154&sourceLongitude=8.6747&destinationLatitude=50.7265&destinationLongitude=7.0895&requestedSeats=2&effectiveUserId=user_456&earliestDepartureTime=2024-03-25T07:00:00Z&latestDepartureTime=2024-03-25T09:00:00Z
```

#### Cancel Trip Request
**Location:** [API_DOCUMENTATION.md § 4.2](./api-reference.md#42-cancel-a-trip)

```json
{
  "tripId": "trip_001",
  "userId": "user_123",
  "reason": "Change of plans"
}
```

**Location in Code:** See `@Schema` example in:
- [`trip-service/src/main/java/.../dto/request/CancelTripRequest.java`](../trip-service/src/main/java/com/cimhub/tripservice/dto/request/CancelTripRequest.java)

---

### Ride Service (Bookings)

#### Join Trip Request
**Location:** [API_DOCUMENTATION.md § 5.1](./api-reference.md#51-join-a-trip-passenger-books)

```json
{
  "passengerId": "user_456",
  "tripId": "trip_001",
  "numberOfSeats": 2,
  "specialRequests": "I have a large suitcase"
}
```

**Location in Code:** See `@Schema` example in:
- [`trip-service/src/main/java/.../dto/request/JoinTripRequest.java`](../trip-service/src/main/java/com/cimhub/tripservice/dto/request/JoinTripRequest.java)

---

## Complete cURL Commands

All ready-to-use cURL commands with full payloads are in:

📍 **[API_INTEGRATION_EXAMPLES.md § cURL Examples](./integration-examples.md#curl-examples)**

Examples include:
- ✅ Authentication (login, signup, validate token)
- ✅ User operations (get profile, create profile)
- ✅ Vehicle operations (register, list)
- ✅ Trip operations (offer, cancel, search)
- ✅ Booking operations (join, cancel, history)

Each includes the full request and response payload.

---

## Complete Code Examples

All working code examples for integration are in:

📍 **[API_INTEGRATION_EXAMPLES.md § JavaScript/Node.js Examples](./integration-examples.md#javascriptnodejs-examples)**

📍 **[API_INTEGRATION_EXAMPLES.md § Python Examples](./integration-examples.md#python-examples)**

📍 **[API_INTEGRATION_EXAMPLES.md § Java Examples](./integration-examples.md#java-examples)**

These include:
- Ready-to-copy code snippets
- API client classes
- Error handling patterns
- Full working examples

---

## Interactive Swagger UI

View all payloads interactively in Swagger UI:

| Service | URL |
|---------|-----|
| Auth Service | http://localhost:8081/auth-service/swagger-ui.html |
| User Service | http://localhost:8082/user-service/swagger-ui.html |
| Vehicle Service | http://localhost:8083/vehicle-service/swagger-ui.html |
| Trip Service | http://localhost:8084/trip-service/swagger-ui.html |
| Ride Service | http://localhost:8085/ride-service/swagger-ui.html |

**In Swagger UI, you can:**
- See all request/response examples from `@Schema` annotations
- Try endpoints directly with "Try it out" button
- See validation rules and constraints
- Download OpenAPI spec

---

## Where Examples Live (Detailed Breakdown)

### In Code (for IDE/Swagger)
```
auth-service/
├── src/main/java/.../dto/request/
│   ├── LoginRequest.java              ← @Schema with example
│   ├── RegisterUserRequest.java       ← @Schema with example
│   └── ValidateTokenRequest.java      ← @Schema with example
└── src/main/java/.../dto/response/
    ├── LoginResponse.java             ← @Schema with example
    ├── SignUpResponse.java            ← @Schema with example
    └── ValidateTokenResponse.java     ← @Schema with example

user-service/
├── src/main/java/.../dto/request/
│   └── CreateProfileRequest.java      ← @Schema with example
└── src/main/java/.../dto/response/
    └── UserResponse.java              ← @Schema with example

vehicle-service/
├── src/main/java/.../dto/request/
│   └── RegisterVehicleRequest.java    ← @Schema with example
└── src/main/java/.../dto/response/
    └── VehicleResponse.java           ← @Schema with example

trip-service/
├── src/main/java/.../dto/request/
│   ├── OfferRideRequest.java         ← @Schema with example
│   └── CancelTripRequest.java        ← @Schema with example
└── src/main/java/.../dto/response/
    └── OfferRideResponse.java        ← @Schema with example
```

### In Markdown (for Reading/Learning)
```
.docs/
├── API_DOCUMENTATION.md           ← Complete endpoint documentation with
│                                     full request/response payloads
├── API_INTEGRATION_EXAMPLES.md    ← Code examples in multiple languages
│                                     with working payloads
├── OPENAPI_SCHEMAS.md             ← Complete schema definitions
│                                     with example values
└── EXAMPLES_LOCATION_GUIDE.md     ← This file
```

---

## How to Use Payloads

### For Development
1. **See examples in code:** Open DTO files and view `@Schema` example
2. **Interactive testing:** Use Swagger UI at http://localhost:PORT/service/swagger-ui.html
3. **Copy & paste:** Use examples from markdown files
4. **cURL testing:** Use commands from API_INTEGRATION_EXAMPLES.md

### For Integration
1. **Start with:** [API_INTEGRATION_EXAMPLES.md](./integration-examples.md)
2. **Find your language:** JavaScript, Python, or Java section
3. **Copy example:** Get working code with payloads
4. **Adapt to your needs:** Replace example values with real data

### For Documentation
1. **Quick reference:** OPENAPI_SCHEMAS.md for field definitions
2. **Complete guide:** API_DOCUMENTATION.md for detailed examples
3. **Real-world scenarios:** API_INTEGRATION_EXAMPLES.md for patterns

---

## Validation & Constraints

Field validation rules are documented in:

📍 **[OPENAPI_SCHEMAS.md § Field Constraints](./openapi-schemas.md#field-constraints)**

📍 **[OPENAPI_SCHEMAS.md § Validation Rules](./openapi-schemas.md#validation-rules)**

Available in both:
- Code `@Schema` descriptions (visible in Swagger)
- Markdown reference tables

---

## Summary

**All payload examples are preserved and easily accessible:**

- ✅ **In Code:** `@Schema` examples visible in Swagger UI and IDE
- ✅ **In Markdown:** Comprehensive examples in organized sections
- ✅ **Interactive:** Swagger UI for live testing
- ✅ **cURL Ready:** Full commands ready to run
- ✅ **Code Samples:** Working examples in multiple languages
- ✅ **Validation:** All constraints documented

**Pick the format that works best for you:**
- Reading/learning? → Use markdown files
- Quick reference? → Use Swagger UI
- Integration? → Use code examples
- Validation rules? → Check markdown tables

**No examples were deleted—they're just organized better!**
