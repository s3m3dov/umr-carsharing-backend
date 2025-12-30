# Trip Service API Documentation

**Base URL:** `/api/trips`  
**Service Port:** `8083`  
**Authentication:** Required (JWT Bearer Token in Authorization header)

---

## Table of Contents
1. [Offer a Ride](#1-offer-a-ride)
2. [Search Trips by Location](#2-search-trips-by-location)
3. [Get Upcoming Rides](#3-get-upcoming-rides-new)

---

## 1. Offer a Ride

Create a new trip offering as a driver.

### Endpoint
```
POST /api/trips/offer
```

### Headers
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

### Request Body
```json
{
  "userId": "driver@example.com",
  "requestContent": {
    "vehicleNumber": "ABC123",
    "sourceAddress": {
      "latitude": 48.3638222,
      "longitude": 10.6866494,
      "placeId": "",
      "placeAddress": "Augsburg (district), Bavaria, Germany"
    },
    "destinationAddress": {
      "latitude": 48.1371079,
      "longitude": 11.5753822,
      "placeId": "",
      "placeAddress": "Munich, Bavaria, Germany"
    },
    "tripStartDateTime": "2025-10-13T20:06:00+02:00",
    "offeredSeat": 3
  }
}
```

### Request DTO Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `userId` | String | Yes | Driver's email/ID (extracted from JWT) |
| `vehicleNumber` | String | Yes | Vehicle registration number |
| `sourceAddress` | Points | Yes | Starting location |
| `sourceAddress.latitude` | Double | Yes | Source latitude |
| `sourceAddress.longitude` | Double | Yes | Source longitude |
| `sourceAddress.placeAddress` | String | Yes | Human-readable source address |
| `sourceAddress.placeId` | String | No | Google Maps Place ID (optional) |
| `destinationAddress` | Points | Yes | Ending location |
| `destinationAddress.latitude` | Double | Yes | Destination latitude |
| `destinationAddress.longitude` | Double | Yes | Destination longitude |
| `destinationAddress.placeAddress` | String | Yes | Human-readable destination address |
| `destinationAddress.placeId` | String | No | Google Maps Place ID (optional) |
| `tripStartDateTime` | String (ISO-8601) | Yes | Trip start time with timezone (e.g., "2025-10-13T20:06:00+02:00") |
| `offeredSeat` | Integer | Yes | Number of seats available (min: 1) |

### Success Response (201 Created)
```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": {
    "tripId": "68ec0a7968f0c407b33556cc",
    "vehicleNumber": "ABC123",
    "sourceAddress": {
      "latitude": 48.3638222,
      "longitude": 10.6866494,
      "placeId": "",
      "placeAddress": "Augsburg (district), Bavaria, Germany"
    },
    "destinationAddress": {
      "latitude": 48.1371079,
      "longitude": 11.5753822,
      "placeId": "",
      "placeAddress": "Munich, Bavaria, Germany"
    },
    "tripStartDateTime": "2025-10-13T20:06:00+02:00",
    "tripTimezone": "Europe/Berlin",
    "routeGeometry": {
      "type": "LineString",
      "coordinates": [
        [10.686807, 48.363787],
        [10.686837, 48.363847],
        ...
        [11.574857, 48.13675]
      ]
    },
    "routeDistanceInMeters": 83654.7,
    "routeDistanceInKm": 83.65,
    "routeDurationInSeconds": 3729.7,
    "routeDurationInMinutes": 62.16,
    "tripCreated": true,
    "errorMessage": null
  }
}
```

### Error Response (400 Bad Request)
```json
{
  "success": false,
  "errorMessage": null,
  "responseContent": {
    "tripId": null,
    "vehicleNumber": null,
    "sourceAddress": null,
    "destinationAddress": null,
    "tripStartDateTime": null,
    "tripTimezone": null,
    "routeGeometry": null,
    "routeDistanceInMeters": null,
    "routeDistanceInKm": null,
    "routeDurationInSeconds": null,
    "routeDurationInMinutes": null,
    "tripCreated": false,
    "errorMessage": "Driver already has a trip at the same time. Please choose a different time."
  }
}
```

### Response DTO Fields

| Field | Type | Description |
|-------|------|-------------|
| `success` | Boolean | Overall request success status |
| `tripId` | String | Unique trip identifier (MongoDB ObjectId) |
| `vehicleNumber` | String | Vehicle registration number |
| `sourceAddress` | Points | Starting location details |
| `destinationAddress` | Points | Ending location details |
| `tripStartDateTime` | String (ISO-8601) | Trip start time in original timezone |
| `tripTimezone` | String | Timezone where trip starts (e.g., "Europe/Berlin", "Asia/Kolkata") |
| `routeGeometry` | GeoJSON LineString | Complete route path coordinates |
| `routeDistanceInMeters` | Double | Total route distance in meters |
| `routeDistanceInKm` | Double | Total route distance in kilometers |
| `routeDurationInSeconds` | Double | Estimated trip duration in seconds |
| `routeDurationInMinutes` | Double | Estimated trip duration in minutes |
| `tripCreated` | Boolean | Whether trip was successfully created |
| `errorMessage` | String | Error message if creation failed |

### Possible Error Messages
- `"Driver already has a trip at the same time. Please choose a different time."`
- `"Could not find a route between source and destination. Please check the addresses."`
- `"Failed to calculate route. Please try again later."`

---

## 2. Search Trips by Location

Search for available trips based on location proximity.

### 2.1 Find Trips Near Source

Find trips starting near a specific location.

#### Endpoint
```
GET /api/trips/search/near-source?latitude=48.1351&longitude=11.5820&radiusKm=10
```

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `latitude` | Double | Yes | - | Search point latitude |
| `longitude` | Double | Yes | - | Search point longitude |
| `radiusKm` | Double | No | 5.0 | Search radius in kilometers |

#### Example Request
```
GET /api/trips/search/near-source?latitude=48.1351&longitude=11.5820&radiusKm=10
Authorization: Bearer <jwt_token>
```

#### Success Response (200 OK)
```json
[
  {
    "tripId": "68ec0a7968f0c407b33556cc",
    "tripStatus": "OFFERED",
    "driverId": "driver@example.com",
    "vehicleNumber": "ABC123",
    "sourceAddress": {
      "latitude": 48.3638222,
      "longitude": 10.6866494,
      "placeAddress": "Augsburg, Germany"
    },
    "destinationAddress": {
      "latitude": 48.1371079,
      "longitude": 11.5753822,
      "placeAddress": "Munich, Germany"
    },
    "tripStartDateTimeUTC": "2025-10-13T18:06:00Z",
    "tripTimezone": "Europe/Berlin",
    "offeredSeat": 3,
    "currSeats": 0,
    "pricePerKm": 10.0,
    "routeDistance": 83654.7,
    "routeDuration": 3729.7
  }
]
```

---

### 2.2 Find Trips Near Destination

Find trips ending near a specific location.

#### Endpoint
```
GET /api/trips/search/near-destination?latitude=48.1351&longitude=11.5820&radiusKm=10
```

#### Query Parameters
Same as "Find Trips Near Source"

---

### 2.3 Find Trips Matching Route

Find trips that match both source and destination within specified radius.

#### Endpoint
```
GET /api/trips/search/matching-route?sourceLat=48.36&sourceLon=10.68&sourceRadiusKm=5&destLat=48.13&destLon=11.57&destRadiusKm=5
```

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `sourceLat` | Double | Yes | - | Source latitude |
| `sourceLon` | Double | Yes | - | Source longitude |
| `sourceRadiusKm` | Double | No | 5.0 | Source search radius in km |
| `destLat` | Double | Yes | - | Destination latitude |
| `destLon` | Double | Yes | - | Destination longitude |
| `destRadiusKm` | Double | No | 5.0 | Destination search radius in km |

#### Example Request
```
GET /api/trips/search/matching-route?sourceLat=48.3638&sourceLon=10.6866&sourceRadiusKm=5&destLat=48.1371&destLon=11.5754&destRadiusKm=5
Authorization: Bearer <jwt_token>
```

---

### 2.4 Find Trips in Area

Find trips starting within a bounding box area.

#### Endpoint
```
GET /api/trips/search/in-area?minLat=47.0&minLon=10.0&maxLat=50.0&maxLon=13.0
```

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `minLat` | Double | Yes | Minimum latitude (southwest corner) |
| `minLon` | Double | Yes | Minimum longitude (southwest corner) |
| `maxLat` | Double | Yes | Maximum latitude (northeast corner) |
| `maxLon` | Double | Yes | Maximum longitude (northeast corner) |

---

## 3. Get Upcoming Rides (NEW)

Get upcoming rides for the authenticated user (either as driver or passenger).

### 3.1 Get My Upcoming Trips (As Driver)

View all upcoming trips you're offering as a driver.

#### Endpoint
```
GET /api/trips/my-trips/upcoming
```

#### Headers
```
Authorization: Bearer <jwt_token>
```

#### Query Parameters
None (user is identified from JWT token)

#### Success Response (200 OK)
```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": [
    {
      "tripId": "68ec0a7968f0c407b33556cc",
      "driverId": "driver@example.com",
      "vehicleNumber": "ABC123",
      "tripStatus": "OFFERED",
      "sourceAddress": {
        "latitude": 48.3638222,
        "longitude": 10.6866494,
        "placeAddress": "Augsburg, Germany"
      },
      "destinationAddress": {
        "latitude": 48.1371079,
        "longitude": 11.5753822,
        "placeAddress": "Munich, Germany"
      },
      "tripStartDateTime": "2025-10-13T20:06:00+02:00",
      "tripTimezone": "Europe/Berlin",
      "offeredSeat": 3,
      "availableSeats": 2,
      "bookedSeats": 1,
      "passengers": [
        {
          "userId": "passenger1@example.com",
          "bookedSeats": 1,
          "pickupLocation": {
            "latitude": 48.35,
            "longitude": 10.70,
            "placeAddress": "Near Augsburg"
          },
          "dropoffLocation": {
            "latitude": 48.15,
            "longitude": 11.56,
            "placeAddress": "Near Munich"
          }
        }
      ],
      "routeDistanceInKm": 83.65,
      "routeDurationInMinutes": 62.16,
      "pricePerKm": 10.0,
      "estimatedEarnings": 836.50
    }
  ]
}
```

---

### 3.2 Get My Upcoming Rides (As Passenger)

View all upcoming rides you've booked as a passenger.

#### Endpoint
```
GET /api/trips/my-rides/upcoming
```

#### Headers
```
Authorization: Bearer <jwt_token>
```

#### Query Parameters
None (user is identified from JWT token)

#### Success Response (200 OK)
```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": [
    {
      "rideId": "68ec0b1234567890abcdef12",
      "tripId": "68ec0a7968f0c407b33556cc",
      "driverId": "driver@example.com",
      "vehicleNumber": "ABC123",
      "rideStatus": "CONFIRMED",
      "pickupLocation": {
        "latitude": 48.35,
        "longitude": 10.70,
        "placeAddress": "Near Augsburg"
      },
      "dropoffLocation": {
        "latitude": 48.15,
        "longitude": 11.56,
        "placeAddress": "Near Munich"
      },
      "tripStartDateTime": "2025-10-13T20:06:00+02:00",
      "tripTimezone": "Europe/Berlin",
      "bookedSeats": 1,
      "rideDistanceInKm": 75.3,
      "rideDurationInMinutes": 55.2,
      "pricePerKm": 10.0,
      "estimatedFare": 753.00,
      "driverDetails": {
        "name": "John Doe",
        "rating": 4.8,
        "totalTrips": 145
      }
    }
  ]
}
```

---

## Common Data Structures

### Points Object
```typescript
{
  latitude: number;      // Required
  longitude: number;     // Required
  placeId?: string;      // Optional - Google Maps Place ID
  placeAddress: string;  // Required - Human-readable address
}
```

### GeoJSON Geometry (Route)
```typescript
{
  type: "LineString";
  coordinates: number[][]; // Array of [longitude, latitude] pairs
}
```

### Timezone Handling
- All times in requests should include timezone: `"2025-10-13T20:06:00+02:00"`
- Server stores times in UTC internally
- Responses include both the original timezone and the time in that timezone
- Frontend can convert to user's local timezone for display

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "errorMessage": "Validation error message",
  "responseContent": null
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "errorMessage": "Invalid or expired token",
  "responseContent": null
}
```

### 404 Not Found
```json
{
  "success": false,
  "errorMessage": "Trip not found",
  "responseContent": null
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "errorMessage": "An unexpected error occurred",
  "responseContent": null
}
```

---

## Testing Examples

### cURL Examples

#### 1. Offer a Ride
```bash
curl -X POST https://api-gateway.example.com/api/trips/offer \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "driver@example.com",
    "requestContent": {
      "vehicleNumber": "ABC123",
      "sourceAddress": {
        "latitude": 48.3638222,
        "longitude": 10.6866494,
        "placeAddress": "Augsburg, Germany"
      },
      "destinationAddress": {
        "latitude": 48.1371079,
        "longitude": 11.5753822,
        "placeAddress": "Munich, Germany"
      },
      "tripStartDateTime": "2025-10-13T20:06:00+02:00",
      "offeredSeat": 3
    }
  }'
```

#### 2. Search for Trips
```bash
curl -X GET "https://api-gateway.example.com/api/trips/search/matching-route?sourceLat=48.36&sourceLon=10.68&destLat=48.13&destLon=11.57" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 3. Get My Upcoming Trips
```bash
curl -X GET "https://api-gateway.example.com/api/trips/my-trips/upcoming" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Frontend Integration Notes

### 1. Date/Time Handling
- Always send times with timezone information
- Use libraries like `date-fns-tz` or `moment-timezone` for timezone handling
- Example: `new Date().toISOString()` → Convert to local timezone format

### 2. Map Integration
- Use `routeGeometry.coordinates` to draw route on map (Leaflet, Google Maps, Mapbox)
- GeoJSON format is directly compatible with most mapping libraries
- Remember: coordinates are `[longitude, latitude]` not `[latitude, longitude]`

### 3. Distance and Duration Display
- Backend provides both meters and kilometers for distance
- Backend provides both seconds and minutes for duration
- Format for user display as needed

### 4. Error Handling
- Always check `success` field first
- Display `errorMessage` to user when `success: false`
- Handle 401 errors by redirecting to login

### 5. Loading States
- Trip creation involves route calculation (OSRM API) - may take 2-3 seconds
- Show loading indicator during API calls

---

## Change Log

### v1.1.0 (October 13, 2025)
- ✅ Added CORS support for frontend domains
- ✅ Fixed tripId and vehicleNumber null issues
- ✅ Added geospatial query support
- ✅ Added upcoming rides endpoints for drivers and passengers
- ✅ Improved timezone handling

### v1.0.0 (October 12, 2025)
- Initial release with offer ride functionality

