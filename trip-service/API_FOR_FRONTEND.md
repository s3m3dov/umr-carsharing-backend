# Car Sharing Backend API Documentation

## Authentication
All endpoints (except login/signup/health) require a JWT token in the `Authorization` header:
```
Authorization: Bearer <JWT_TOKEN>
```

## Endpoints

### 1. Offer a Ride
- **POST** `/api/trips/offer`
- **Headers:** `Authorization: Bearer <JWT_TOKEN>`
- **Body:**
```json
{
  "vehicleNumber": "string",
  "sourceAddress": { "latitude": float, "longitude": float, "placeAddress": "string" },
  "destinationAddress": { "latitude": float, "longitude": float, "placeAddress": "string" },
  "tripStartDateTime": "ISO8601 string",
  "tripTimezone": "string",
  "routeGeometry": { "type": "LineString", "coordinates": [[lon, lat], ...] },
  ...other fields...
}
```
- **Response:**
```json
{
  "success": true,
  "responseContent": { "tripId": "string", ... }
}
```

### 2. Find a Ride
- **POST** `/api/rides/find-ride`
- **Headers:** `Authorization: Bearer <JWT_TOKEN>`
- **Body:**
```json
{
  "userId": "user1@example.com",
  "requestContent": {
    "pickupPoint": {
      "latitude": 48.3638222,
      "longitude": 10.6866494,
      "placeAddress": "Augsburg (district), Bavaria, Germany"
    },
    "destinationPoint": {
      "latitude": 48.1371079,
      "longitude": 11.5753822,
      "placeAddress": "Munich, Bavaria, Germany"
    },
    "rideStartTime": "2025-10-14T10:13",
    "requestedSeats": 1
  }
}
```
- **Response:**
```json
{
  "success": true,
  "responseContent": [
    {
      "tripId": "string",
      "driverEmail": "string",
      "vehicleNumber": "string",
      "sourceAddress": { "latitude": 48.3638222, "longitude": 10.6866494, "placeAddress": "Augsburg" },
      "destinationAddress": { "latitude": 48.1371079, "longitude": 11.5753822, "placeAddress": "Munich" },
      "tripStartDateTime": "2025-10-14T10:00:00",
      "availableSeats": 3,
      ...
    }
  ]
}
```

### 3. Upcoming Trips (Driver)
- **GET** `/api/trips/my-trips/upcoming`
- **Headers:** `Authorization: Bearer <JWT_TOKEN>`, `X-User-Id: <userId>`
- **Response:**
```json
{
  "success": true,
  "responseContent": [ { "tripId": "string", ... }, ... ]
}
```

### 4. Upcoming Rides (Passenger)
- **GET** `/api/trips/my-rides/upcoming`
- **Headers:** `Authorization: Bearer <JWT_TOKEN>`, `X-User-Id: <userId>`
- **Response:**
```json
{
  "success": true,
  "responseContent": [ { "rideId": "string", ... }, ... ]
}
```

## DTOs
- See backend code for full DTOs: `DriverUpcomingTripDTO`, `PassengerUpcomingRideDTO`, etc.
- All location fields for geospatial queries use GeoJSON format:
```json
{ "type": "Point", "coordinates": [longitude, latitude] }
```

## Notes
- Always send JWT in `Authorization` header.
- For upcoming rides/trips, the backend expects `X-User-Id` (can be extracted from JWT by gateway).
- CORS is enabled for all origins.

---
For questions, contact backend team.
