# API Development Guide

This guide provides a comprehensive overview of how to interact with the Car Sharing platform's APIs.

## 🔑 Authentication

All protected endpoints require an internal JWT (JSON Web Token) in the `Authorization` header. **Firebase tokens are NOT supported for direct API calls.**

```http
Authorization: Bearer <your_jwt_token>
```

- **Login/Signup**: Use the `/api/auth/login` and `/api/auth/signup` endpoints in the **Auth Service** to obtain a token.
- **Service Identity**: The API Gateway validates the JWT, extracts the identity, and passes it to internal services via the `X-User-Id` and `X-User-Role` headers.
- **Note to Mobile Developers**: Do not use the Firebase Flutter SDK for authentication. Call the backend APIs directly to manage user sessions.

## 📡 Common Response Format

All API responses follow a standard envelope:

**Success:**
```json
{
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2026-04-05T12:00:00Z"
}
```

**Error:**
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error description"
  },
  "message": "An error occurred",
  "timestamp": "2026-04-05T12:00:00Z"
}
```

> `data` and `error` are mutually exclusive — only one is present per response.

## 🛠️ Technical Implementation Details

### Timezone Handling

- **Storage**: All times are stored in **UTC** internally.
- **Input/Output**: APIs use **ISO-8601** with timezone offsets (e.g., `2025-10-13T20:06:00+02:00`).
- **Frontend Recommendation**: Always detect the user's timezone using `Intl.DateTimeFormat().resolvedOptions().timeZone` and display converted times.

### Geospatial Logic

The system uses MongoDB's `2dsphere` indexes for proximity searches. Large datasets are queried efficiently within milliseconds.

### Route Calculation (OSRM)

The Trip Service integrates with the OpenStreetMap Routing Machine (OSRM) to:

- Generate complete route geometries (GeoJSON LineStrings).
- Calculate accurate driving distances and durations.

## 🔍 Review Service

The Review Service manages mutual reviews and ratings between drivers and passengers after completed trips, enabling trust-building within the platform.

### Authentication

All review endpoints require JWT authentication via the `Authorization: Bearer <token>` header.

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/reviews/driver` | Passenger submits a review for a driver |
| POST | `/api/reviews/passenger` | Driver submits a review for a passenger |
| GET | `/api/reviews/driver/{driverId}` | Get all reviews about a driver |
| GET | `/api/reviews/passenger/{passengerId}` | Get all reviews about a passenger |
| GET | `/api/reviews/driver/{driverId}/average-rating` | Get average rating for a driver |
| GET | `/api/reviews/passenger/{passengerId}/average-rating` | Get average rating for a passenger |

### POST /api/reviews/driver

Passenger submits a review for a driver after completing a trip.

**Request Body:**

```json
{
  "userId": "jane.passenger@example.com",
  "userRole": "PASSENGER",
  "tripId": "trip_123",
  "bookingId": "booking_001",
  "rating": 5,
  "comment": "Great driver, very friendly and professional!"
}
```

**Response (201 Created):**

```json
{
  "data": {
    "reviewId": "review_789",
    "tripId": "trip_123",
    "bookingId": "booking_001",
    "reviewerId": "jane.passenger@example.com",
    "reviewerType": "PASSENGER",
    "revieweeId": "john.driver@example.com",
    "revieweeType": "DRIVER",
    "rating": 5,
    "comment": "Great driver, very friendly and professional!",
    "status": "PUBLISHED",
    "createdAt": "2026-04-05T10:00:00.000Z"
  },
  "message": "Operation successful",
  "timestamp": "2026-04-05T10:00:00Z"
}
```

### POST /api/reviews/passenger

Driver submits a review for a passenger. Same request/response structure as above with `userRole: "DRIVER"`.

### GET /api/reviews/driver/{driverId}

Retrieve all reviews for a specific driver. Returns a list using the same envelope with `data` as an array.

### GET /api/reviews/driver/{driverId}/average-rating

Get the average rating for a driver.

**Response:**

```json
{
  "data": {
    "subjectId": "john.driver@example.com",
    "averageRating": 4.8,
    "totalReviews": 25
  },
  "message": "Operation successful",
  "timestamp": "2026-04-05T10:00:00Z"
}
```

---

## 📖 Full API Documentation (Swagger)

For a complete and interactive list of all endpoints, parameters, and models, use the **Aggregated Swagger UI**:

- **URL**: `http://localhost:8080/swagger-ui.html`
- Use the dropdown at the top to switch between microservices (User, Vehicle, Trip, Ride, Review, Notification).
