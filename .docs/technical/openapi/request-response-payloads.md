# Request & Response Payloads - Complete Map

Both **REQUEST payloads** (what to send) and **RESPONSE payloads** (what you get back) are now imported from centralized ApiExamples files and visible in Swagger UI.

## How It Works

### REQUEST Examples
- **Defined in:** Each service's `ApiExamples.java` as `*_REQUEST` constants
- **Used in:** Request DTOs with `@Schema(example = ApiExamples.YOUR_REQUEST)`
- **Visible in:** Swagger UI → endpoint → **Request body** section

### RESPONSE Examples
- **Defined in:** Each service's `ApiExamples.java` as `*_RESPONSE` constants
- **Used in:** Controller with `@ApiResponse` → `@ExampleObject(value = ApiExamples.YOUR_RESPONSE)`
- **Visible in:** Swagger UI → endpoint → **Responses** section

---

## Auth Service Payloads

### Login
**REQUEST** - What to send:
```json
{
  "email": "john.driver@example.com",
  "password": "SecurePassword123!"
}
```
Source: `ApiExamples.LOGIN_REQUEST` → imported in `LoginRequest.java` DTO

**RESPONSE** - What you get back:
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
Source: `ApiExamples.LOGIN_RESPONSE` → imported in `AuthController` annotation

---

### Register (Signup)
**REQUEST** - What to send:
```json
{
  "firstName": "John",
  "lastName": "Driver",
  "email": "john.driver@example.com",
  "password": "SecurePassword123!",
  "userType": "DRIVER",
  "phoneNumber": "+49 123 456789",
  "age": 32,
  "licenseNumber": "LD-123456789"
}
```
Source: `ApiExamples.SIGNUP_REQUEST` → imported in `RegisterUserRequest.java` DTO

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "john.driver@example.com",
    "role": "DRIVER"
  },
  "message": "User registered successfully"
}
```
Source: `ApiExamples.SIGNUP_RESPONSE` → imported in `AuthController` annotation

---

### Validate Token
**REQUEST** - What to send:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyIsInJvbGUiOiJEUklWRVIiLCJleHAiOjE2OTk4NzcyMDB9.signature"
}
```
Source: `ApiExamples.VALIDATE_TOKEN_REQUEST` → imported in `ValidateTokenRequest.java` DTO

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": {
    "isValid": true,
    "email": "john.driver@example.com",
    "role": "DRIVER"
  },
  "message": "Token is valid"
}
```
Source: `ApiExamples.VALIDATE_TOKEN_RESPONSE` → imported in `AuthController` annotation

---

## User Service Payloads

### Get User Profile
**REQUEST** - Path parameter:
```
GET /api/users/user_123
```

**RESPONSE** - What you get back:
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
Source: `ApiExamples.GET_USER_RESPONSE` → imported in `UserController` annotation

---

### Create Profile
**REQUEST** - What to send:
```json
{
  "userId": "user_123",
  "firstName": "John",
  "lastName": "Driver",
  "phoneNumber": "+49 123 456789",
  "age": 32
}
```
Source: `ApiExamples.CREATE_PROFILE_REQUEST` → imported in `CreateProfileRequest.java` DTO

**RESPONSE** - What you get back:
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
    "profileCompletedAt": "2024-01-15T10:35:00Z"
  },
  "message": "User profile created successfully"
}
```
Source: `ApiExamples.CREATE_PROFILE_RESPONSE` → imported in `UserController` annotation

---

## Vehicle Service Payloads

### Register Vehicle
**REQUEST** - What to send:
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
Source: `ApiExamples.REGISTER_VEHICLE_REQUEST` → imported in `RegisterVehicleRequest.java` DTO

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": null,
  "message": "Vehicle registered successfully"
}
```
Source: `ApiExamples.REGISTER_VEHICLE_RESPONSE` → imported in `VehicleController` annotation

---

### Get User Vehicles
**REQUEST** - Path parameter:
```
GET /api/vehicles/user_123
```

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": [
    {
      "value": "vehicle_001",
      "text": "Tesla Model 3 - HE-XX-1234",
      "seatingCapacity": "5"
    },
    {
      "value": "vehicle_002",
      "text": "BMW 3 Series - HE-XX-5678",
      "seatingCapacity": "5"
    }
  ],
  "message": "Vehicles retrieved successfully"
}
```
Source: `ApiExamples.GET_VEHICLES_RESPONSE` → imported in `VehicleController` annotation

---

## Trip Service Payloads

### Offer Trip (Create)
**REQUEST** - What to send:
```json
{
  "driverId": "user_123",
  "vehicleNumber": "HE-XX-1234",
  "sourceAddress": {
    "latitude": 50.8154,
    "longitude": 8.6747
  },
  "destinationAddress": {
    "latitude": 50.7265,
    "longitude": 7.0895
  },
  "tripStartDateTime": "2024-03-25T08:00:00Z",
  "totalSeats": 3
}
```
Source: `ApiExamples.OFFER_TRIP_REQUEST` → imported in `OfferRideRequest.java` DTO

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": {
    "tripId": "trip_001",
    "driverId": "user_123",
    "departureTime": "2024-03-25T08:00:00Z",
    "availableSeats": 3,
    "status": "SCHEDULED"
  },
  "message": "Trip created successfully"
}
```
Source: `ApiExamples.OFFER_TRIP_RESPONSE` → imported in `TripController` annotation

---

### Cancel Trip
**REQUEST** - What to send:
```json
{
  "tripId": "trip_001",
  "userId": "user_123",
  "cancellationReason": "Change of plans"
}
```
Source: `ApiExamples.CANCEL_TRIP_REQUEST` → imported in `CancelTripRequest.java` DTO

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": "Trip cancelled successfully",
  "message": "Trip cancelled"
}
```
Source: `ApiExamples.CANCEL_TRIP_RESPONSE` → imported in `TripController` annotation

---

### Search Trips
**REQUEST** - Query parameters:
```
GET /api/trips/search/matching-route?
  sourceLatitude=50.8154&
  sourceLongitude=8.6747&
  destinationLatitude=50.7265&
  destinationLongitude=7.0895&
  requestedSeats=2&
  effectiveUserId=user_456&
  earliestDepartureTime=2024-03-25T07:00:00Z&
  latestDepartureTime=2024-03-25T09:00:00Z
```

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": [
    {
      "tripId": "trip_001",
      "driverId": "user_123",
      "departureTime": "2024-03-25T08:00:00Z",
      "sourceLocation": {
        "latitude": 50.8154,
        "longitude": 8.6747
      },
      "destinationLocation": {
        "latitude": 50.7265,
        "longitude": 7.0895
      },
      "availableSeats": 3,
      "pricePerSeat": 15.50
    }
  ],
  "message": "Trips found"
}
```
Source: `ApiExamples.SEARCH_TRIPS_RESPONSE` → imported in `TripController` annotation

---

### Get Active Trips
**REQUEST** - Path parameter:
```
GET /api/trips/active/driver/user_123
```

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": [
    {
      "tripId": "trip_001",
      "status": "SCHEDULED",
      "departureTime": "2024-03-25T08:00:00Z",
      "destinationLocation": {
        "latitude": 50.7265,
        "longitude": 7.0895
      },
      "currentPassengers": 2,
      "availableSeats": 1
    }
  ],
  "message": "Active trips retrieved"
}
```
Source: `ApiExamples.ACTIVE_TRIPS_RESPONSE` → imported in `TripController` annotation

---

## Review Service Payloads

### Submit Driver Review
**REQUEST** - What to send:
```json
{
  "userId": "user_456",
  "userRole": "PASSENGER",
  "tripId": "trip_001",
  "bookingId": "booking_001",
  "rating": 5,
  "comment": "Great driver, very comfortable ride!"
}
```
Source: `ReviewApiExamples.SUBMIT_REVIEW_REQUEST` → imported in `SubmitReviewRequest.java` DTO

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": {
    "reviewId": "review_001",
    "tripId": "trip_001",
    "bookingId": "booking_001",
    "reviewerId": "user_456",
    "reviewerType": "PASSENGER",
    "revieweeId": "user_123",
    "revieweeType": "DRIVER",
    "rating": 5,
    "comment": "Great driver, very comfortable ride!",
    "status": "PUBLISHED",
    "createdAt": "2026-03-25T10:00:00.000Z",
    "updatedAt": "2026-03-25T10:00:00.000Z"
  },
  "message": "Review submitted successfully"
}
```
Source: `ReviewApiExamples.SUBMIT_DRIVER_REVIEW_RESPONSE` → imported in `ReviewController` annotation

---

### Get Driver Reviews
**REQUEST** - Path parameter:
```
GET /api/reviews/driver/user_123
```

**RESPONSE** - What you get back:
```json
{
  "success": true,
  "data": [
    {
      "reviewId": "review_001",
      "tripId": "trip_001",
      "bookingId": "booking_001",
      "reviewerId": "user_456",
      "reviewerType": "PASSENGER",
      "revieweeId": "user_123",
      "revieweeType": "DRIVER",
      "rating": 5,
      "comment": "Great driver!",
      "status": "PUBLISHED",
      "createdAt": "2026-03-25T10:00:00.000Z",
      "updatedAt": "2026-03-25T10:00:00.000Z"
    }
  ],
  "message": "Reviews retrieved successfully"
}
```
Source: `ReviewApiExamples.GET_DRIVER_REVIEWS_RESPONSE` → imported in `ReviewController` annotation

---

### Admin Trip Detail
**REQUEST** - Path parameter:
```
GET /api/admin/trips/{tripId}
```

**RESPONSE schema note:** `AdminTripResponse` includes audit timestamps in UTC datetime format:
- `createdAt` (e.g., `2026-03-30T12:00:00.000Z`)
- `updatedAt` (e.g., `2026-03-30T12:10:30.123Z`)

Source: `trip-service/.../dto/response/AdminTripResponse.java`

---

### Admin Vehicle Detail
**REQUEST** - Path parameter:
```
GET /api/admin/vehicles/{vehicleId}
```

**RESPONSE schema note:** `AdminVehicleResponse` includes audit timestamps in UTC datetime format:
- `createdAt` (e.g., `2026-03-30T12:00:00.000Z`)
- `updatedAt` (e.g., `2026-03-30T12:05:45.456Z`)

Source: `user-service/.../dto/response/AdminVehicleResponse.java`

---

## Where Everything Is

### REQUEST Payloads
Located in each service's `ApiExamples.java`:
- ✅ `ApiExamples.LOGIN_REQUEST`
- ✅ `ApiExamples.SIGNUP_REQUEST`
- ✅ `ApiExamples.VALIDATE_TOKEN_REQUEST`
- ✅ `ApiExamples.CREATE_PROFILE_REQUEST`
- ✅ `ApiExamples.REGISTER_VEHICLE_REQUEST`
- ✅ `ApiExamples.OFFER_TRIP_REQUEST`
- ✅ `ApiExamples.CANCEL_TRIP_REQUEST`
- ✅ `ReviewApiExamples.SUBMIT_REVIEW_REQUEST`

**Imported in:** Corresponding request DTOs with `@Schema(example = ApiExamples.YOUR_REQUEST)`

### RESPONSE Payloads
Located in each service's `ApiExamples.java`:
- ✅ `ApiExamples.LOGIN_RESPONSE`
- ✅ `ApiExamples.SIGNUP_RESPONSE`
- ✅ `ApiExamples.VALIDATE_TOKEN_RESPONSE`
- ✅ `ApiExamples.GET_USER_RESPONSE`
- ✅ `ApiExamples.CREATE_PROFILE_RESPONSE`
- ✅ `ApiExamples.GET_VEHICLES_RESPONSE`
- ✅ `ApiExamples.REGISTER_VEHICLE_RESPONSE`
- ✅ `ApiExamples.OFFER_TRIP_RESPONSE`
- ✅ `ApiExamples.SEARCH_TRIPS_RESPONSE`
- ✅ `ApiExamples.ACTIVE_TRIPS_RESPONSE`
- ✅ `ApiExamples.CANCEL_TRIP_RESPONSE`
- ✅ `ReviewApiExamples.SUBMIT_DRIVER_REVIEW_RESPONSE`
- ✅ `ReviewApiExamples.SUBMIT_PASSENGER_REVIEW_RESPONSE`
- ✅ `ReviewApiExamples.GET_DRIVER_REVIEWS_RESPONSE`
- ✅ `ReviewApiExamples.GET_PASSENGER_REVIEWS_RESPONSE`

Also documented directly via response DTO schemas:
- ✅ `AdminTripResponse` (`createdAt`, `updatedAt`)
- ✅ `AdminVehicleResponse` (`createdAt`, `updatedAt`)
- ✅ `ReviewResponse` (`createdAt`, `updatedAt`)

**Imported in:** Controller endpoints with `@ExampleObject(value = ApiExamples.YOUR_RESPONSE)`

---

## View in Swagger UI

1. **Start service:**
   ```bash
   cd {service-name} && mvn spring-boot:run
   ```

2. **Open Swagger UI:**
   ```
   http://localhost:8081/auth-service/swagger-ui.html
   ```

3. **Click any endpoint** (e.g., `POST /api/auth/login`)

4. **See both sections:**
   - **Request body** - Shows REQUEST example from DTO `@Schema`
   - **Responses** - Shows RESPONSE examples from `@ApiResponse`

---

## Update Workflow

### To Update a REQUEST Example
1. Find constant in service's `ApiExamples.java` (e.g., `LOGIN_REQUEST`)
2. Update the JSON string
3. Service automatically uses updated example (restart to reload)

### To Update a RESPONSE Example
1. Find constant in service's `ApiExamples.java` (e.g., `LOGIN_RESPONSE`)
2. Update the JSON string
3. Service automatically uses updated example (restart to reload)

---

## Summary

✅ **REQUEST payloads** - Imported in request DTOs, visible in Swagger request section
✅ **RESPONSE payloads** - Imported in controller annotations, visible in Swagger responses section
✅ **Easy to maintain** - Update examples in one place (ApiExamples.java)
✅ **Clean code** - No examples embedded in annotations
✅ **Swagger UI** - Both request and response examples visible
✅ **Single source of truth** - All payloads organized by service
