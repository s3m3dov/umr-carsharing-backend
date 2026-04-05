# Car Sharing API Documentation

## Overview

This is the comprehensive API documentation for the Car Sharing microservices platform. The platform is built using Spring Boot microservices architecture with the following components:

- **Auth Service**: Authentication and JWT token management
- **User Service**: User profile and vehicle management
- **Trip Service**: Trip creation, searching, booking, and management
- **Notification Service**: User notifications
- **Review Service**: Driver and passenger reviews

All APIs return standardized JSON responses and support OpenAPI 3.0 documentation.

## API Base URLs

All requests go through the API Gateway on **port 8080**. The path prefix routes to the correct service and is stripped before forwarding.

| Service | Gateway Path Prefix | OpenAPI Spec |
|---------|---------------------|--------------|
| Auth Service | `http://localhost:8080/auth-service` | `http://localhost:8080/auth-service/v3/api-docs` |
| User Service | `http://localhost:8080/user-service` | `http://localhost:8080/user-service/v3/api-docs` |
| Trip Service | `http://localhost:8080/trip-service` | `http://localhost:8080/trip-service/v3/api-docs` |
| Review Service | `http://localhost:8080/review-service` | `http://localhost:8080/review-service/v3/api-docs` |
| Notification Service | `http://localhost:8080/notification-service` | `http://localhost:8080/notification-service/v3/api-docs` |

Aggregated Swagger UI: `http://localhost:8080/swagger-ui.html`

## Authentication

All protected endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer <JWT_TOKEN>
```

The JWT token is obtained by logging in or signing up through the Auth Service.

### Token Structure

JWT tokens contain the following claims:
- `sub`: User ID
- `email`: User email address
- `role`: User role (DRIVER or PASSENGER)
- `exp`: Token expiration timestamp

## Standard Response Format

All API responses follow a standardized format:

### Success Response
```json
{
  "success": true,
  "data": {
    "field1": "value1",
    "field2": "value2"
  },
  "message": "Operation completed successfully"
}
```

### Error Response
```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "Human-readable error message"
}
```

## Common HTTP Status Codes

| Status | Meaning | Usage |
|--------|---------|-------|
| 200 | OK | Successful GET, PUT, DELETE requests |
| 201 | Created | Successful resource creation |
| 400 | Bad Request | Invalid input or validation errors |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists or constraint violation |
| 500 | Internal Server Error | Server error |

---

# Service APIs

## 1. Auth Service API

The Auth Service handles user authentication, registration, and token validation.

### Endpoints

#### 1.1 Register User (Sign Up)

**POST** `/api/auth/signup`

Creates a new user account and returns a JWT token.

**Request:**
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

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyIsIuuuu",
    "email": "john.driver@example.com",
    "role": "DRIVER"
  },
  "message": "User registered successfully"
}
```

**Error Responses:**
- 400: Invalid email format or missing required fields
- 409: User with this email already exists

**Notes:**
- `userType` must be either "DRIVER" or "PASSENGER"
- For DRIVER accounts, vehicle information is recommended
- Password must be strong (minimum 8 characters recommended)

---

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

**Error Responses:**
- 401: Invalid email or password
- 400: Missing required fields

**Notes:**
- Token is valid for 24 hours (configurable)
- Use returned token in Authorization header for subsequent requests

---

#### 1.3 Validate Token

**POST** `/api/auth/validate`

Validates a JWT token and returns user information.

**Request:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
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

**Error Responses:**
- 401: Token is invalid or expired

**Notes:**
- Used internally by API Gateway for request validation
- Can be called without Authorization header

---

#### 1.4 Initiate Password Reset

**POST** `/api/auth/reset-password`

Sends a reset token to the user's email.

**Request:**
```json
{ "email": "john.driver@example.com" }
```

**Response (200 OK):** Returns reset token info.

---

#### 1.5 Confirm Password Reset

**POST** `/api/auth/reset-password/confirm`

Resets password using the token received by email.

**Request:**
```json
{
  "email": "john.driver@example.com",
  "resetToken": "abc123",
  "newPassword": "NewSecurePassword123!"
}
```

**Error Responses:**
- 401: Invalid or expired reset token

---

#### 1.6 Change Password

**PUT** `/api/auth/change-password`

Changes the password for the currently authenticated user. Requires the current password.

**Request:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewSecurePassword123!"
}
```

**Response (200 OK):** Confirmation message.

**Error Responses:**
- 401: Current password is incorrect or user not authenticated
- 400: Missing fields or password too short

**Authentication:** Required

---

## 2. User Service API

The User Service manages user profiles and personal information.

### Endpoints

#### 2.1 Get User Information

**GET** `/api/users/{userId}`

Retrieves user profile information.

**Parameters:**
- `userId` (path, required): User's unique identifier

**Response (200 OK):**
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

**Error Responses:**
- 404: User not found

**Authentication:** Required

---

#### 2.2 Create User Profile

**POST** `/api/users/profile`

Creates or updates a user's detailed profile.

**Request:**
```json
{
  "userId": "user_123",
  "firstName": "John",
  "lastName": "Driver",
  "age": 32,
  "phoneNumber": "+49 123 456789"
}
```

**Response (201 Created):**
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

**Error Responses:**
- 400: Invalid profile data
- 404: User not found

**Authentication:** Required

---

## 3. Vehicle Management (User Service)

Vehicle management is now part of the User Service and handles vehicle registration and management for drivers.

### Endpoints

#### 3.1 Get User Vehicles

**GET** `/api/vehicles/{userId}`

Retrieves all vehicles registered by a user.

**Parameters:**
- `userId` (path, required): Driver's unique identifier

**Response (200 OK):**
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

**Error Responses:**
- 404: No vehicles found for the user

**Authentication:** Required

---

#### 3.2 Register Vehicle

**POST** `/api/vehicles/register`

Registers a new vehicle for a driver.

**Request:**
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

**Response (201 Created):**
```json
{
  "success": true,
  "data": null,
  "message": "Vehicle registered successfully"
}
```

**Error Responses:**
- 400: Invalid input or vehicle already registered
- 409: License plate already in use or maximum vehicles exceeded

**Vehicle Types:**
- `sedan`: 4-door sedan
- `suv`: Sport utility vehicle
- `minivan`: Minivan
- `hatchback`: Hatchback

**Authentication:** Required

---

## 4. Trip Service API

The Trip Service manages trip creation, searching, and history for drivers and passengers.

### Endpoints

#### 4.1 Offer a Trip (Driver creates a ride)

**POST** `/api/trips/offer`

A driver creates a new trip offer.

**Request:**
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

**Response (201 Created):**
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

**Error Responses:**
- 400: Invalid trip data
- 401: Unauthorized

**Authentication:** Required (DRIVER role)

---

#### 4.2 Cancel a Trip

**POST** `/api/trips/cancel`

Cancels a trip (driver) or booking (passenger).

**Request:**
```json
{
  "tripId": "trip_001",
  "userId": "user_123",
  "reason": "Change of plans"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": "Trip cancelled successfully",
  "message": "Operation completed"
}
```

**Error Responses:**
- 404: Trip not found
- 409: Trip cannot be cancelled (already started)

**Authentication:** Required

---

#### 4.3 Search Trips Near Pickup Location

**GET** `/api/trips/search/near-source`

Finds trips with pickup location near specified coordinates.

**Query Parameters:**
- `latitude` (required): Pickup latitude, e.g., `50.8154`
- `longitude` (required): Pickup longitude, e.g., `8.6747`
- `radiusKm` (optional, default: 5.0): Search radius in km, e.g., `10.0`

**Example Request:**
```
GET /api/trips/search/near-source?latitude=50.8154&longitude=8.6747&radiusKm=10
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "tripId": "trip_001",
      "driverId": "user_123",
      "departureTime": "2024-03-25T08:00:00Z",
      "sourceLocation": {"latitude": 50.8154, "longitude": 8.6747},
      "destinationLocation": {"latitude": 50.7265, "longitude": 7.0895},
      "availableSeats": 3,
      "pricePerSeat": 15.50
    }
  ],
  "message": "Trips found"
}
```

**Authentication:** Required

---

#### 4.4 Search Trips Near Destination

**GET** `/api/trips/search/near-destination`

Finds trips with dropoff location near specified coordinates.

**Query Parameters:**
- `latitude` (required): Destination latitude
- `longitude` (required): Destination longitude
- `radiusKm` (optional, default: 5.0): Search radius in km

**Authentication:** Required

---

#### 4.5 Search Trips Matching Route

**GET** `/api/trips/search/matching-route`

Advanced search for trips matching a passenger's desired route.

**Query Parameters:**
- `sourceLatitude` (required): Pickup location latitude
- `sourceLongitude` (required): Pickup location longitude
- `sourceRadiusKm` (optional, default: 5.0): Pickup search radius
- `destinationLatitude` (required): Dropoff location latitude
- `destinationLongitude` (required): Dropoff location longitude
- `destinationRadiusKm` (optional, default: 5.0): Dropoff search radius
- `earliestDepartureTime` (optional): Earliest departure time (ISO 8601)
- `latestDepartureTime` (optional): Latest departure time (ISO 8601)
- `requestedSeats` (required): Number of seats needed
- `effectiveUserId` (required): Passenger's user ID

**Example Request:**
```
GET /api/trips/search/matching-route?sourceLatitude=50.8154&sourceLongitude=8.6747&destinationLatitude=50.7265&destinationLongitude=7.0895&requestedSeats=2&effectiveUserId=user_456&earliestDepartureTime=2024-03-25T07:00:00Z&latestDepartureTime=2024-03-25T09:00:00Z
```

**Authentication:** Required

---

#### 4.6 Get Driver's Active Trips

**GET** `/api/trips/active/driver/{driverId}`

Retrieves all active trips for a driver.

**Parameters:**
- `driverId` (path, required): Driver's user ID

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "tripId": "trip_001",
      "status": "SCHEDULED",
      "departureTime": "2024-03-25T08:00:00Z",
      "destinationLocation": {"latitude": 50.7265, "longitude": 7.0895},
      "currentPassengers": 2,
      "availableSeats": 1
    }
  ],
  "message": "Active trips retrieved"
}
```

**Authentication:** Required

---

#### 4.7 Get Driver's Trip History

**GET** `/api/trips/history/driver/{driverId}`

Retrieves completed and cancelled trips for a driver.

**Parameters:**
- `driverId` (path, required): Driver's user ID

**Authentication:** Required

---

## 5. Ride Management — Passenger (`/api/rides`)

The `RideController` handles the passenger side of trip participation.

### Endpoints

#### 5.1 Book a Ride (Passenger)

**POST** `/api/rides/book`

Passenger books a seat on an existing trip. Creates a pending booking waiting for driver acceptance.

**Request:**
```json
{
  "passengerId": "user_456",
  "tripId": "trip_001",
  "numberOfSeats": 2
}
```

**Response (201 Created):**
```json
{
  "data": "Ride booked successfully",
  "message": "Operation successful",
  "timestamp": "2026-04-05T10:00:00Z"
}
```

**Error Responses:**
- 404: Trip not found
- 409: No available seats

**Authentication:** Required

---

#### 5.2 Cancel a Ride (Passenger)

**POST** `/api/rides/cancel`

Passenger cancels their booked ride.

**Request:**
```json
{
  "rideId": "ride_001",
  "userId": "user_456"
}
```

**Error Responses:**
- 404: Ride not found
- 409: Trip already in progress

**Authentication:** Required

---

#### 5.3 Get Passenger's Active Rides

**GET** `/api/rides/active/passenger/{passengerId}`

Retrieves confirmed and upcoming rides for a passenger.

**Authentication:** Required

---

#### 5.4 Get Passenger's Ride History

**GET** `/api/rides/history/passenger/{passengerId}`

Retrieves completed and cancelled rides for a passenger.

**Authentication:** Required

---

## 5b. Booking Management — Driver (`/api/bookings`)

The `BookingController` lets drivers accept or reject incoming booking requests.

### Endpoints

#### 5b.1 Accept a Booking

**POST** `/api/bookings/accept?bookingId={bookingId}`

Driver accepts a passenger's booking request, confirming the ride.

**Response (200 OK):**
```json
{
  "data": "Booking accepted",
  "message": "Operation successful",
  "timestamp": "2026-04-05T10:00:00Z"
}
```

**Error Responses:**
- 404: Booking not found

**Authentication:** Required (DRIVER role)

---

#### 5b.2 Reject a Booking

**POST** `/api/bookings/reject?bookingId={bookingId}`

Driver rejects a passenger's booking request.

**Authentication:** Required (DRIVER role)

---

## 6. Review Service API

The Review Service manages driver and passenger reviews after trip completion. The service uses path-based endpoints to distinguish between driver and passenger reviews.

### Endpoints

#### 6.1 Submit a Review for a Driver (Passenger)

**POST** `/api/reviews/driver`

Passenger submits a review for a driver after completing a trip.

**Request:**
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

**Response (201 Created):**
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

**Error Responses:**
- 400: Invalid review data or trip not completed
- 404: Driver or user not found
- 409: Review already exists for this trip
- 403: Unauthorized to review (not the passenger of this booking)

**Authentication:** Required (PASSENGER role)

---

#### 6.2 Submit a Review for a Passenger (Driver)

**POST** `/api/reviews/passenger`

Driver submits a review for a passenger after completing a trip.

**Request:**
```json
{
  "userId": "user_123",
  "userRole": "DRIVER",
  "tripId": "trip_001",
  "bookingId": "booking_001",
  "rating": 4,
  "comment": "Good passenger, on time and polite"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "reviewId": "review_002",
    "tripId": "trip_001",
    "bookingId": "booking_001",
    "reviewerId": "user_123",
    "reviewerType": "DRIVER",
    "revieweeId": "user_456",
    "revieweeType": "PASSENGER",
    "rating": 4,
    "comment": "Good passenger, on time and polite",
    "status": "PUBLISHED",
    "createdAt": "2026-03-25T10:30:00.000Z",
    "updatedAt": "2026-03-25T10:30:00.000Z"
  },
  "message": "Review submitted successfully"
}
```

**Error Responses:**
- 400: Invalid review data or trip not completed
- 404: Passenger or user not found
- 409: Review already exists for this trip
- 403: Unauthorized to review (not the driver of this trip)

**Authentication:** Required (DRIVER role)

---

#### 6.3 Get All Reviews for a Driver

**GET** `/api/reviews/driver/{driverId}`

Retrieves all reviews about a specific driver.

**Parameters:**
- `driverId` (path, required): Driver's unique identifier

**Response (200 OK):**
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

**Error Responses:**
- 404: Driver not found or no reviews exist

**Authentication:** Required

---

#### 6.4 Get All Reviews for a Passenger

**GET** `/api/reviews/passenger/{passengerId}`

Retrieves all reviews about a specific passenger.

**Parameters:**
- `passengerId` (path, required): Passenger's unique identifier

**Error Responses:**
- 404: Passenger not found or no reviews exist

**Authentication:** Required

---

#### 6.5 Get Driver's Average Rating

**GET** `/api/reviews/driver/{driverId}/average-rating`

Calculates and returns the average rating for a driver.

**Parameters:**
- `driverId` (path, required): Driver's unique identifier

**Response (200 OK):**
```json
{
  "data": {
    "subjectId": "user_123",
    "averageRating": 4.75,
    "totalReviews": 4
  },
  "message": "Average rating retrieved successfully",
  "timestamp": "2026-04-05T10:00:00Z"
}
```

**Error Responses:**
- 404: Driver not found

**Authentication:** Required

---

#### 6.6 Get Passenger's Average Rating

**GET** `/api/reviews/passenger/{passengerId}/average-rating`

Calculates and returns the average rating for a passenger.

**Parameters:**
- `passengerId` (path, required): Passenger's unique identifier

**Error Responses:**
- 404: Passenger not found

**Authentication:** Required

---

### Review Validation Rules

1. **Trip Completion**: Both trip and booking must be in COMPLETED status
2. **Participant Validation**: 
   - Only passengers can review drivers
   - Only drivers can review passengers
   - Reviewer must be the actual passenger/driver of the specific booking
3. **One Review Per Booking**: A user can only submit one review per trip/booking combination

**Note:** The TripServiceClient in review-service returns false silently on errors. This may mask service unavailability issues in some edge cases.

---

## 7. Notification Service API

The Notification Service manages user notifications with WebSocket delivery and MongoDB persistence.

### Endpoints

#### 7.1 Create a Notification

**POST** `/api/notifications`

Creates a notification and publishes it over WebSocket.

**Headers:**
- `X-User-Id` (optional): Authenticated user ID (provided by gateway)
- `X-User-Role` (optional): Authenticated user role

**Request:**
```json
{
  "recipientId": "user_456",
  "senderId": "user_123",
  "title": "Booking Accepted",
  "message": "Your booking for trip_001 has been accepted.",
  "type": "BOOKING_ACCEPTED",
  "relatedEntityId": "trip_001"
}
```

**Response (201 Created):**
```json
{
  "data": {
    "notificationId": "notif_001",
    "recipientId": "user_456",
    "title": "Booking Accepted",
    "message": "Your booking for trip_001 has been accepted.",
    "read": false,
    "createdAt": "2026-04-05T10:00:00Z"
  },
  "message": "Operation successful",
  "timestamp": "2026-04-05T10:00:00Z"
}
```

**Error Responses:**
- 400: Invalid sender or request payload
- 403: Cannot create notifications on behalf of another user

**Authentication:** Required

---

#### 7.2 List Notifications for a Recipient

**GET** `/api/notifications/{recipientId}?page=0&size=20`

Returns paginated notifications ordered by latest first. Users can only access their own; admins can access any.

**Parameters:**
- `recipientId` (path, required): Recipient's user ID
- Standard Spring `Pageable` query params: `page`, `size`, `sort`

**Headers:**
- `X-User-Id` (required)

**Authentication:** Required

---

#### 7.3 Mark a Notification as Read

**PATCH** `/api/notifications/{notificationId}/read`

**Headers:**
- `X-User-Id` (required)

**Request:**
```json
{ "recipientId": "user_456" }
```

**Error Responses:**
- 404: Notification not found

**Authentication:** Required

---

#### 7.4 Mark All Notifications as Read

**PATCH** `/api/notifications/{recipientId}/read-all`

Marks all unread notifications for a recipient as read.

**Headers:**
- `X-User-Id` (required)

**Authentication:** Required

---

#### 7.5 Get Notification Stats

**GET** `/api/notifications/{recipientId}/stats`

Returns aggregated notification counters (total, unread).

**Headers:**
- `X-User-Id` (required)

**Authentication:** Required

---

# Advanced Examples

## Example 1: Complete User Journey (Driver)

### Step 1: Register as Driver
```bash
curl -X POST http://localhost:8080/auth-service/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Driver",
    "email": "john.driver@example.com",
    "password": "SecurePassword123!",
    "userType": "DRIVER",
    "phoneNumber": "+49 123 456789",
    "age": 32,
    "licenseNumber": "LD-123456789"
  }'
```

Response includes JWT token: `eyJhbGc...`

### Step 2: Register Vehicle
```bash
curl -X POST http://localhost:8080/user-service/api/vehicles/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGc..." \
  -d '{
    "userId": "user_123",
    "vehicleName": "Tesla Model 3",
    "vehicleNumber": "HE-XX-1234",
    "vehicleType": "sedan",
    "vehicleColor": "white",
    "seatingCapacity": "5"
  }'
```

### Step 3: Offer a Trip
```bash
curl -X POST http://localhost:8080/trip-service/api/trips/offer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGc..." \
  -d '{
    "driverId": "user_123",
    "vehicleId": "vehicle_001",
    "sourceLatitude": 50.8154,
    "sourceLongitude": 8.6747,
    "destinationLatitude": 50.7265,
    "destinationLongitude": 7.0895,
    "departureTime": "2024-03-25T08:00:00Z",
    "availableSeats": 3,
    "pricePerSeat": 15.50
  }'
```

---

## Example 2: Complete User Journey (Passenger)

### Step 1: Register as Passenger
```bash
curl -X POST http://localhost:8080/auth-service/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Passenger",
    "email": "jane.passenger@example.com",
    "password": "SecurePassword456!",
    "userType": "PASSENGER",
    "phoneNumber": "+49 987 654321",
    "age": 28
  }'
```

### Step 2: Search for Matching Trips
```bash
curl -X GET "http://localhost:8080/trip-service/api/trips/search/matching-route?sourceLatitude=50.8150&sourceLongitude=8.6750&destinationLatitude=50.7260&destinationLongitude=7.0890&requestedSeats=2&effectiveUserId=user_456" \
  -H "Authorization: Bearer eyJhbGc..."
```

### Step 3: Book a Ride
```bash
curl -X POST http://localhost:8080/trip-service/api/rides/book \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGc..." \
  -d '{
    "passengerId": "user_456",
    "tripId": "trip_001",
    "numberOfSeats": 2
  }'
```

---

# Error Handling

## Error Codes

| Code | Meaning | HTTP Status |
|------|---------|------------|
| `VALIDATION_ERROR` | Input validation failed | 400 |
| `INVALID_CREDENTIALS` | Invalid email or password | 401 |
| `INVALID_TOKEN` | JWT token is invalid or expired | 401 |
| `USER_NOT_FOUND` | User does not exist | 404 |
| `USER_ALREADY_EXISTS` | User with email already registered | 409 |
| `VEHICLE_NOT_FOUND` | Vehicle does not exist | 404 |
| `VEHICLE_ALREADY_EXISTS` | Vehicle already registered | 409 |
| `TRIP_NOT_FOUND` | Trip does not exist | 404 |
| `TRIP_FULL` | Trip has no available seats | 409 |
| `UNAUTHORIZED` | User lacks permission for operation | 403 |

---

# Rate Limiting

The APIs implement rate limiting to prevent abuse:

- **Default**: 100 requests per minute per user
- **Auth endpoints**: 10 requests per minute (to prevent brute force)

Rate limit information is returned in response headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1711428000
```

---

# Pagination

For endpoints returning lists, pagination is implemented using:

- `page` (query param): Page number (0-indexed)
- `size` (query param): Items per page
- `sort` (query param): Sort order (e.g., `departureTime:desc`)

**Example:**
```
GET /api/trips/active/driver/user_123?page=0&size=20&sort=departureTime:desc
```

---

# Versioning

The API follows semantic versioning. Current version: **1.0.0**

Version is included in:
- OpenAPI specification at `v3/api-docs`
- Service info in OpenAPI UI

---

# Support and Feedback

For issues, feature requests, or feedback on the API:

- Contact: Hikmat Samadov (Samadov@students.uni-marburg.de)
- License: Apache 2.0

---

## Last Updated

- April 2026
- Includes all microservices and endpoints: auth, user, vehicle, trip, ride, booking, review, notification
