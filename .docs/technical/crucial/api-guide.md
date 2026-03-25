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

```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": { ... }
}
```

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
| POST | `/api/reviews` | Submit a review |
| GET | `/api/reviews/driver/{driverId}` | Get reviews about a driver |
| GET | `/api/reviews/user/{userId}` | Get reviews about a passenger |
| GET | `/api/reviews/trip/{tripId}` | Get all reviews for a trip |
| GET | `/api/reviews/given` | Get reviews I submitted (authenticated user) |
| GET | `/api/reviews/received` | Get reviews I received (authenticated user) |
| GET | `/api/reviews/driver/{driverId}/average-rating` | Get average rating for a driver |

### POST /api/reviews

Submit a review for a driver or passenger after a completed trip.

**Request Body:**

```json
{
  "tripId": "trip_123",
  "revieweeId": "user_456",
  "rating": 5,
  "comment": "Great driver, very friendly and professional!"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": {
    "id": "review_789",
    "tripId": "trip_123",
    "reviewerId": "user_123",
    "revieweeId": "user_456",
    "rating": 5,
    "comment": "Great driver, very friendly and professional!",
    "createdAt": "2025-10-13T20:06:00+02:00"
  }
}
```

### GET /api/reviews/driver/{driverId}

Retrieve all reviews for a specific driver.

**Response:**

```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": [
    {
      "id": "review_789",
      "tripId": "trip_123",
      "reviewerId": "user_123",
      "rating": 5,
      "comment": "Great driver, very friendly and professional!",
      "createdAt": "2025-10-13T20:06:00+02:00"
    }
  ]
}
```

### GET /api/reviews/user/{userId}

Retrieve all reviews for a specific passenger.

**Response:** Same format as driver reviews endpoint.

### GET /api/reviews/trip/{tripId}

Get all reviews (driver and passenger) associated with a completed trip.

**Response:**

```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": [
    {
      "id": "review_789",
      "tripId": "trip_123",
      "reviewerId": "driver_123",
      "revieweeId": "passenger_456",
      "rating": 5,
      "comment": "Great passenger!",
      "createdAt": "2025-10-13T20:06:00+02:00"
    }
  ]
}
```

### GET /api/reviews/given

Retrieve all reviews submitted by the authenticated user.

**Headers:** Requires `Authorization: Bearer <token>`

**Response:** Same format as other review list endpoints.

### GET /api/reviews/received

Retrieve all reviews received by the authenticated user (about them).

**Headers:** Requires `Authorization: Bearer <token>`

**Response:** Same format as other review list endpoints.

### GET /api/reviews/driver/{driverId}/average-rating

Get the average rating for a driver.

**Response:**

```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": {
    "driverId": "user_456",
    "averageRating": 4.8,
    "totalReviews": 25
  }
}
```

---

## 📖 Full API Documentation (Swagger)

For a complete and interactive list of all endpoints, parameters, and models, use the **Aggregated Swagger UI**:

- **URL**: `http://localhost:8080/swagger-ui.html`
- Use the dropdown at the top to switch between microservices (User, Vehicle, Trip, Ride, Review, Notification).
