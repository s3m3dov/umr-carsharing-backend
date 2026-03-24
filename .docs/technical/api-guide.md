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

## 📖 Full API Documentation (Swagger)

For a complete and interactive list of all endpoints, parameters, and models, use the **Aggregated Swagger UI**:

- **URL**: `http://localhost:8080/swagger-ui.html`
- Use the dropdown at the top to switch between microservices (User, Vehicle, Trip, Ride, Notification).
