# Timezone Handling in Trip Service - Frontend Integration Guide

## Problem Solved
**Question:** If a ride is booked in India, will someone in Germany see Indian time or German time?

**Answer:** They will see the CORRECT time converted to their timezone! 

## How It Works

### 1. **Storage (Backend)**
- All trip times are stored in **UTC (Universal Time)** in the database
- We also store the original timezone (e.g., "Asia/Kolkata" for India)
- This ensures consistency across all timezones

### 2. **API Request Format**
When creating a ride, send time with timezone information:

```json
{
  "VehicleNumber": "MH12AB1234",
  "sourceAddress": {...},
  "destinationAddress": {...},
  "tripStartDateTime": "2025-10-15T14:30:00+05:30",
  "offeredSeat": 4
}
```

**Format:** `yyyy-MM-dd'T'HH:mm:ssXXX`
- `+05:30` = India Standard Time (IST)
- `+02:00` = Central European Summer Time (CEST)
- `+00:00` = UTC
- `-05:00` = Eastern Standard Time (EST)

### 3. **API Response Format**
The response includes both the zoned time and timezone:

```json
{
  "tripId": "123",
  "tripStartDateTime": "2025-10-15T14:30:00+05:30",
  "tripTimezone": "Asia/Kolkata",
  "tripCreated": true
}
```

### 4. **Frontend Conversion**
Your frontend can convert this to the user's local timezone:

```javascript
// Example in JavaScript
const tripTime = "2025-10-15T14:30:00+05:30"; // From API
const userLocalTime = new Date(tripTime).toLocaleString(); 

// User in India sees: "15/10/2025, 2:30:00 PM"
// User in Germany sees: "15/10/2025, 11:00:00 AM"
// User in USA (EST) sees: "15/10/2025, 5:00:00 AM"
```

---

## 📚 API Documentation for Frontend Team

### Base URL
```
http://your-api-gateway:port/trip-service
```

---

## 🚗 API Endpoint 1: Offer a Ride

### Endpoint
```
POST /api/trips/offer
```

### Description
Allows a driver to offer a new ride. The system checks for time conflicts and prevents double-booking.

### Request Headers
```
Content-Type: application/json
Authorization: Bearer <token>
```

### Request Body Structure

#### OfferRideRequestDTO
```json
{
  "userId": "driver123",
  "requestContent": {
    "VehicleNumber": "MH12AB1234",
    "sourceAddress": {
      "latitude": 19.0760,
      "longitude": 72.8777,
      "placeId": "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
      "placeAddress": "Mumbai, Maharashtra, India"
    },
    "destinationAddress": {
      "latitude": 18.5204,
      "longitude": 73.8567,
      "placeId": "ChIJARFGZy6_wjsRQ-Oenb9DjYI",
      "placeAddress": "Pune, Maharashtra, India"
    },
    "tripStartDateTime": "2025-12-25T14:30:00+05:30",
    "offeredSeat": 3
  }
}
```

#### Field Details:

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `userId` | String | Yes | Driver's user ID (from JWT token) | "driver123" |
| `VehicleNumber` | String | Yes | Vehicle registration number | "MH12AB1234" |
| `sourceAddress` | Object | Yes | Starting point with coordinates | See Points structure below |
| `destinationAddress` | Object | Yes | Destination with coordinates | See Points structure below |
| `tripStartDateTime` | String | Yes | Trip start time with timezone (ISO-8601) | "2025-12-25T14:30:00+05:30" |
| `offeredSeat` | Integer | Yes | Number of seats offered (min: 1) | 3 |

#### Points Object Structure:
```json
{
  "latitude": 19.0760,
  "longitude": 72.8777,
  "placeId": "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
  "placeAddress": "Mumbai, Maharashtra, India"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `latitude` | Double | Yes | Latitude coordinate |
| `longitude` | Double | Yes | Longitude coordinate |
| `placeId` | String | No | Google Places ID (for accuracy) |
| `placeAddress` | String | No | Human-readable address |

### Response Body Structure

#### Success Response (201 Created)
```json
{
  "responseContent": {
    "tripId": "507f1f77bcf86cd799439011",
    "vehicleNumber": "MH12AB1234",
    "sourceAddress": {
      "latitude": 19.0760,
      "longitude": 72.8777,
      "placeId": "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
      "placeAddress": "Mumbai, Maharashtra, India"
    },
    "destinationAddress": {
      "latitude": 18.5204,
      "longitude": 73.8567,
      "placeId": "ChIJARFGZy6_wjsRQ-Oenb9DjYI",
      "placeAddress": "Pune, Maharashtra, India"
    },
    "tripStartDateTime": "2025-12-25T14:30:00+05:30",
    "tripTimezone": "Asia/Kolkata",
    "tripCreated": true,
    "errorMessage": null
  }
}
```

#### Error Response (400 Bad Request) - Time Conflict
```json
{
  "responseContent": {
    "tripId": null,
    "vehicleNumber": null,
    "sourceAddress": null,
    "destinationAddress": null,
    "tripStartDateTime": null,
    "tripTimezone": null,
    "tripCreated": false,
    "errorMessage": "Driver already has a trip at the same time. Please choose a different time."
  }
}
```

### Response Fields:

| Field | Type | Description |
|-------|------|-------------|
| `tripId` | String | Unique trip identifier (MongoDB ObjectId) |
| `vehicleNumber` | String | Vehicle registration number |
| `sourceAddress` | Object | Starting point details |
| `destinationAddress` | Object | Destination details |
| `tripStartDateTime` | String | Trip start time with timezone offset |
| `tripTimezone` | String | IANA timezone ID (e.g., "Asia/Kolkata") |
| `tripCreated` | Boolean | Success flag |
| `errorMessage` | String | Error description (null if successful) |

### Business Logic:
- ✅ Prevents drivers from booking overlapping trips (within 1-hour window)
- ✅ Stores time in UTC for global consistency
- ✅ Validates all required fields
- ✅ Auto-generates unique trip ID
- ✅ Sets initial status as "OFFERED"

---

## 🔍 API Endpoint 2: Search Available Trips

### Endpoint
```
GET /api/trips/search?userTimezone={timezone}
```

### Description
Returns all available trips with times converted to the user's timezone.

### Request Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `userTimezone` | String | Yes | User's IANA timezone | "Europe/Berlin", "Asia/Kolkata" |

### Example Request
```
GET /api/trips/search?userTimezone=Europe/Berlin
```

### Response Body Structure

#### TripViewDTO Array
```json
[
  {
    "tripId": "507f1f77bcf86cd799439011",
    "driverId": "driver123",
    "vehicleNumber": "MH12AB1234",
    "sourceAddress": {
      "latitude": 19.0760,
      "longitude": 72.8777,
      "placeId": "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
      "placeAddress": "Mumbai, Maharashtra, India"
    },
    "destinationAddress": {
      "latitude": 18.5204,
      "longitude": 73.8567,
      "placeId": "ChIJARFGZy6_wjsRQ-Oenb9DjYI",
      "placeAddress": "Pune, Maharashtra, India"
    },
    "tripStartDateTime": "2025-12-25T14:30:00+05:30",
    "tripTimezone": "Asia/Kolkata",
    "tripStartDateTimeInUserTimezone": "2025-12-25T10:00:00+01:00",
    "userTimezone": "Europe/Berlin",
    "offeredSeat": 3,
    "availableSeats": 3,
    "tripStatus": "OFFERED",
    "pricePerKm": 10.0
  }
]
```

### Response Fields:

| Field | Type | Description |
|-------|------|-------------|
| `tripId` | String | Unique trip identifier |
| `driverId` | String | Driver's user ID |
| `vehicleNumber` | String | Vehicle registration number |
| `sourceAddress` | Object | Trip starting point |
| `destinationAddress` | Object | Trip destination |
| `tripStartDateTime` | String | **Original trip time** (in trip's timezone) |
| `tripTimezone` | String | **Timezone where trip occurs** |
| `tripStartDateTimeInUserTimezone` | String | **Trip time in user's timezone** |
| `userTimezone` | String | **User's timezone** |
| `offeredSeat` | Integer | Total seats offered |
| `availableSeats` | Integer | Remaining available seats |
| `tripStatus` | String | Trip status: "OFFERED", "IN_PROGRESS", "COMPLETED", "CANCELLED" |
| `pricePerKm` | Double | Price per kilometer |

---

## 🕐 Timezone Handling - Frontend Implementation

### How to Get User's Timezone

#### JavaScript/TypeScript
```javascript
// Get user's timezone automatically
const userTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
// Returns: "Asia/Kolkata", "Europe/Berlin", "America/New_York", etc.
```

### How to Format DateTime for API Request

#### JavaScript (sending to backend)
```javascript
// When user selects a date and time
const selectedDate = new Date("2025-12-25T14:30:00");

// Get user's timezone offset
const userTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

// Format with timezone for API
const formatter = new Intl.DateTimeFormat('en-US', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  timeZone: userTimezone,
  timeZoneName: 'longOffset'
});

// Or use a library like date-fns or moment.js
import { format } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';

const tripDateTime = formatInTimeZone(
  selectedDate,
  userTimezone,
  "yyyy-MM-dd'T'HH:mm:ssXXX"
);
// Result: "2025-12-25T14:30:00+05:30"
```

### How to Display DateTime from API Response

#### Option 1: Show in User's Local Time
```javascript
// From API response
const tripTime = "2025-12-25T14:30:00+05:30";

// Convert to user's local time
const date = new Date(tripTime);
const localTime = date.toLocaleString('en-US', {
  dateStyle: 'full',
  timeStyle: 'short'
});
// Output: "Wednesday, December 25, 2025 at 10:00 AM" (for German user)
```

#### Option 2: Show Both Times
```javascript
// Show original trip time and user's local time
const tripTime = "2025-12-25T14:30:00+05:30";
const tripTimezone = "Asia/Kolkata";

const date = new Date(tripTime);

// Original time
const originalTime = date.toLocaleString('en-US', {
  timeZone: tripTimezone,
  dateStyle: 'medium',
  timeStyle: 'short'
});
// "Dec 25, 2025, 2:30 PM IST"

// User's local time
const userLocalTime = date.toLocaleString('en-US', {
  dateStyle: 'medium',
  timeStyle: 'short'
});
// "Dec 25, 2025, 10:00 AM CET"

// Display both
console.log(`Trip starts at ${originalTime} (${userLocalTime} your time)`);
```

### React Example Component

```jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function TripSearchComponent() {
  const [trips, setTrips] = useState([]);
  const [userTimezone, setUserTimezone] = useState('');

  useEffect(() => {
    // Get user's timezone
    const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    setUserTimezone(timezone);

    // Fetch trips
    fetchTrips(timezone);
  }, []);

  const fetchTrips = async (timezone) => {
    try {
      const response = await axios.get(
        `/api/trips/search?userTimezone=${timezone}`
      );
      setTrips(response.data);
    } catch (error) {
      console.error('Error fetching trips:', error);
    }
  };

  const formatTripTime = (tripDateTime, tripTimezone) => {
    const date = new Date(tripDateTime);
    
    // Original time where trip happens
    const originalTime = date.toLocaleString('en-US', {
      timeZone: tripTimezone,
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

    // User's local time
    const localTime = date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

    return { originalTime, localTime };
  };

  return (
    <div>
      <h2>Available Trips</h2>
      {trips.map((trip) => {
        const { originalTime, localTime } = formatTripTime(
          trip.tripStartDateTime,
          trip.tripTimezone
        );

        return (
          <div key={trip.tripId} className="trip-card">
            <h3>{trip.sourceAddress.placeAddress} → {trip.destinationAddress.placeAddress}</h3>
            <p>Driver: {trip.driverId}</p>
            <p>Vehicle: {trip.vehicleNumber}</p>
            <p>
              <strong>Departure:</strong> {originalTime} (local)
              <br />
              <small>({localTime} your time)</small>
            </p>
            <p>Available Seats: {trip.availableSeats} / {trip.offeredSeat}</p>
            <p>Price: ₹{trip.pricePerKm}/km</p>
          </div>
        );
      })}
    </div>
  );
}

export default TripSearchComponent;
```

---

## 📝 Data Models Reference

### Complete Trip Entity (Database)
```java
{
  "tripId": "507f1f77bcf86cd799439011",
  "tripStatus": "OFFERED",
  "vehicleNumber": "MH12AB1234",
  "driverId": "driver123",
  "sourceAddress": { /* Points object */ },
  "destinationAddress": { /* Points object */ },
  "offeredSeat": 3,
  "currSeats": 0,
  "tripStartDateTimeUTC": "2025-12-25T09:00:00Z",  // Stored in UTC
  "tripTimezone": "Asia/Kolkata",
  "pricePerKm": 10.0,
  "joinedRidersId": [],
  "createdDate": "2025-10-12T10:30:00Z"
}
```

### Trip Status Values
- `OFFERED` - Trip is available for booking
- `IN_PROGRESS` - Trip has started
- `COMPLETED` - Trip has ended
- `CANCELLED` - Trip was cancelled

---

## 📋 Example Scenarios

### Scenario 1: Indian Driver Books a Ride
**Step 1: Driver creates ride**
```json
POST /api/trips/offer
{
  "userId": "driver_mumbai",
  "requestContent": {
    "VehicleNumber": "MH12AB1234",
    "tripStartDateTime": "2025-12-25T14:30:00+05:30",
    "offeredSeat": 3,
    // ... other fields
  }
}
```

**Backend Processing:**
- Receives: `2025-12-25T14:30:00+05:30` (2:30 PM IST)
- Stores in DB: `2025-12-25T09:00:00Z` (UTC)
- Saves timezone: `Asia/Kolkata`

**Step 2: German user searches**
```
GET /api/trips/search?userTimezone=Europe/Berlin
```

**Response:**
```json
{
  "tripStartDateTime": "2025-12-25T14:30:00+05:30",  // Original
  "tripTimezone": "Asia/Kolkata",
  "tripStartDateTimeInUserTimezone": "2025-12-25T10:00:00+01:00",  // Converted
  "userTimezone": "Europe/Berlin"
}
```

**Frontend Display:**
```
Trip departs at: Dec 25, 2:30 PM IST (10:00 AM your time)
```

---

## 🌍 Common Timezone Codes

| Region | Timezone ID | Offset (Winter) | Offset (Summer) |
|--------|-------------|-----------------|-----------------|
| **India** | Asia/Kolkata | +05:30 | +05:30 |
| **Germany** | Europe/Berlin | +01:00 | +02:00 |
| **UK** | Europe/London | +00:00 | +01:00 |
| **USA (East)** | America/New_York | -05:00 | -04:00 |
| **USA (West)** | America/Los_Angeles | -08:00 | -07:00 |
| **Japan** | Asia/Tokyo | +09:00 | +09:00 |
| **Australia** | Australia/Sydney | +10:00 | +11:00 |
| **UAE** | Asia/Dubai | +04:00 | +04:00 |

---

## ⚠️ Important Notes for Frontend Team

### DO's ✅
1. **Always include timezone** when sending datetime to backend
2. **Use ISO-8601 format** with timezone offset: `yyyy-MM-dd'T'HH:mm:ssXXX`
3. **Auto-detect user's timezone** using `Intl.DateTimeFormat().resolvedOptions().timeZone`
4. **Display both times** - original trip time and user's local time
5. **Use date libraries** like `date-fns-tz` or `moment-timezone` for complex operations
6. **Test with different timezones** - India, USA, Europe, etc.

### DON'Ts ❌
1. **Don't send dates without timezone** - Backend will reject them
2. **Don't do manual timezone math** - Use built-in browser APIs
3. **Don't assume user is in specific timezone** - Always detect
4. **Don't forget DST** - Daylight Saving Time affects offsets
5. **Don't display only UTC** - Users won't understand it

### Validation Rules
- `tripStartDateTime` must be in future
- `offeredSeat` must be >= 1
- `vehicleNumber` is required
- Both `sourceAddress` and `destinationAddress` must have valid lat/long

---

## 🧪 Testing Examples

### Test Case 1: Create Ride from India
```bash
curl -X POST http://localhost:8080/trip-service/api/trips/offer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": "test_driver",
    "requestContent": {
      "VehicleNumber": "MH12AB1234",
      "sourceAddress": {
        "latitude": 19.0760,
        "longitude": 72.8777,
        "placeAddress": "Mumbai, India"
      },
      "destinationAddress": {
        "latitude": 18.5204,
        "longitude": 73.8567,
        "placeAddress": "Pune, India"
      },
      "tripStartDateTime": "2025-12-25T14:30:00+05:30",
      "offeredSeat": 3
    }
  }'
```

### Test Case 2: Search from Germany
```bash
curl -X GET "http://localhost:8080/trip-service/api/trips/search?userTimezone=Europe/Berlin" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📞 Support & Questions

If you have questions about:
- **Timezone conversion** - Check the JavaScript examples above
- **API integration** - See the React component example
- **Date formatting** - Use `date-fns-tz` library
- **Validation errors** - Check the field requirements table

**Backend Team Contact:** [Your contact info]

---

## 🔄 Changelog

**Version 1.0 - October 2025**
- ✅ Implemented timezone-aware storage (UTC)
- ✅ Added timezone information to all datetime fields
- ✅ Created offer ride endpoint with conflict detection
- ✅ Created search endpoint with timezone conversion
- ✅ Added comprehensive documentation for frontend team

---

**Last Updated:** October 12, 2025  
**Backend Service:** trip-service  
**API Version:** v1
