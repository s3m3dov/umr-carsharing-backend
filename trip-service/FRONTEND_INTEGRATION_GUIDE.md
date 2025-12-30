# Frontend Integration Guide - Upcoming Rides Feature

## Overview
This guide explains how to integrate the new upcoming rides feature that allows:
- **Drivers** to view their upcoming trips (rides they're offering)
- **Passengers** to view their upcoming rides (rides they've booked)

---

## Quick Start

### For Drivers - View My Upcoming Trips

**Endpoint:** `GET /api/trips/my-trips/upcoming`

**Headers:**
```javascript
{
  'Authorization': 'Bearer <jwt_token>',
  'X-User-Id': 'driver@example.com'  // Driver's user ID
}
```

**Response Example:**
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
      "passengers": [],
      "routeDistanceInKm": 83.65,
      "routeDurationInMinutes": 62.16,
      "pricePerKm": 10.0,
      "estimatedEarnings": 836.50
    }
  ]
}
```

---

### For Passengers - View My Upcoming Rides

**Endpoint:** `GET /api/trips/my-rides/upcoming`

**Headers:**
```javascript
{
  'Authorization': 'Bearer <jwt_token>',
  'X-User-Id': 'passenger@example.com'  // Passenger's user ID
}
```

**Response Example:**
```json
{
  "success": true,
  "errorMessage": null,
  "responseContent": []
}
```

**Note:** Passenger rides will be empty until the booking system is implemented (Phase 2).

---

## React/TypeScript Implementation Examples

### 1. TypeScript Types

```typescript
// types/trips.ts

export interface Points {
  latitude: number;
  longitude: number;
  placeId?: string;
  placeAddress: string;
}

export interface DriverUpcomingTrip {
  tripId: string;
  driverId: string;
  vehicleNumber: string;
  tripStatus: string;
  sourceAddress: Points;
  destinationAddress: Points;
  tripStartDateTime: string; // ISO-8601 format
  tripTimezone: string;
  offeredSeat: number;
  availableSeats: number;
  bookedSeats: number;
  passengers: PassengerInfo[];
  routeDistanceInKm: number;
  routeDurationInMinutes: number;
  pricePerKm: number;
  estimatedEarnings: number;
}

export interface PassengerInfo {
  userId: string;
  bookedSeats: number;
  pickupLocation: Points;
  dropoffLocation: Points;
}

export interface PassengerUpcomingRide {
  rideId: string;
  tripId: string;
  driverId: string;
  vehicleNumber: string;
  rideStatus: string;
  pickupLocation: Points;
  dropoffLocation: Points;
  tripStartDateTime: string;
  tripTimezone: string;
  bookedSeats: number;
  rideDistanceInKm: number;
  rideDurationInMinutes: number;
  pricePerKm: number;
  estimatedFare: number;
  driverDetails?: {
    name: string;
    rating: number;
    totalTrips: number;
  };
}

export interface ApiResponse<T> {
  success: boolean;
  errorMessage: string | null;
  responseContent: T;
}
```

---

### 2. API Service

```typescript
// services/tripService.ts

import axios from 'axios';
import { DriverUpcomingTrip, PassengerUpcomingRide, ApiResponse } from '../types/trips';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'https://api-gateway-yvbz.onrender.com';

// Get JWT token from localStorage or your auth store
const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  const userId = localStorage.getItem('userId');
  
  return {
    'Authorization': `Bearer ${token}`,
    'X-User-Id': userId,
    'Content-Type': 'application/json'
  };
};

export const tripService = {
  /**
   * Get upcoming trips for the driver
   */
  async getMyUpcomingTrips(): Promise<DriverUpcomingTrip[]> {
    try {
      const response = await axios.get<ApiResponse<DriverUpcomingTrip[]>>(
        `${API_BASE_URL}/api/trips/my-trips/upcoming`,
        { headers: getAuthHeaders() }
      );
      
      if (response.data.success) {
        return response.data.responseContent;
      } else {
        throw new Error(response.data.errorMessage || 'Failed to fetch trips');
      }
    } catch (error) {
      console.error('Error fetching upcoming trips:', error);
      throw error;
    }
  },

  /**
   * Get upcoming rides for the passenger
   */
  async getMyUpcomingRides(): Promise<PassengerUpcomingRide[]> {
    try {
      const response = await axios.get<ApiResponse<PassengerUpcomingRide[]>>(
        `${API_BASE_URL}/api/trips/my-rides/upcoming`,
        { headers: getAuthHeaders() }
      );
      
      if (response.data.success) {
        return response.data.responseContent;
      } else {
        throw new Error(response.data.errorMessage || 'Failed to fetch rides');
      }
    } catch (error) {
      console.error('Error fetching upcoming rides:', error);
      throw error;
    }
  }
};
```

---

### 3. React Component - Driver's Upcoming Trips

```typescript
// components/DriverUpcomingTrips.tsx

import React, { useEffect, useState } from 'react';
import { tripService } from '../services/tripService';
import { DriverUpcomingTrip } from '../types/trips';
import { format, parseISO } from 'date-fns';

export const DriverUpcomingTrips: React.FC = () => {
  const [trips, setTrips] = useState<DriverUpcomingTrip[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadUpcomingTrips();
  }, []);

  const loadUpcomingTrips = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await tripService.getMyUpcomingTrips();
      setTrips(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load trips');
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateTimeStr: string) => {
    return format(parseISO(dateTimeStr), 'MMM dd, yyyy hh:mm a');
  };

  if (loading) {
    return <div className="loading">Loading your upcoming trips...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>Error: {error}</p>
        <button onClick={loadUpcomingTrips}>Retry</button>
      </div>
    );
  }

  if (trips.length === 0) {
    return (
      <div className="empty-state">
        <p>You don't have any upcoming trips.</p>
        <button onClick={() => window.location.href = '/offer-ride'}>
          Offer a Ride
        </button>
      </div>
    );
  }

  return (
    <div className="upcoming-trips">
      <h2>My Upcoming Trips ({trips.length})</h2>
      
      {trips.map((trip) => (
        <div key={trip.tripId} className="trip-card">
          <div className="trip-header">
            <span className="vehicle">{trip.vehicleNumber}</span>
            <span className={`status ${trip.tripStatus.toLowerCase()}`}>
              {trip.tripStatus}
            </span>
          </div>

          <div className="trip-route">
            <div className="location">
              <span className="label">From:</span>
              <span className="address">{trip.sourceAddress.placeAddress}</span>
            </div>
            <div className="location">
              <span className="label">To:</span>
              <span className="address">{trip.destinationAddress.placeAddress}</span>
            </div>
          </div>

          <div className="trip-details">
            <div className="detail-item">
              <span className="icon">🕒</span>
              <span>{formatDateTime(trip.tripStartDateTime)}</span>
            </div>
            <div className="detail-item">
              <span className="icon">📏</span>
              <span>{trip.routeDistanceInKm.toFixed(1)} km</span>
            </div>
            <div className="detail-item">
              <span className="icon">⏱️</span>
              <span>{Math.round(trip.routeDurationInMinutes)} min</span>
            </div>
          </div>

          <div className="trip-seats">
            <div className="seats-info">
              <span>Offered: {trip.offeredSeat}</span>
              <span>Booked: {trip.bookedSeats}</span>
              <span>Available: {trip.availableSeats}</span>
            </div>
            <div className="earnings">
              Estimated Earnings: €{trip.estimatedEarnings.toFixed(2)}
            </div>
          </div>

          {trip.passengers && trip.passengers.length > 0 && (
            <div className="passengers">
              <h4>Passengers:</h4>
              <ul>
                {trip.passengers.map((passenger, idx) => (
                  <li key={idx}>
                    {passenger.userId} - {passenger.bookedSeats} seat(s)
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="trip-actions">
            <button onClick={() => viewTripDetails(trip.tripId)}>
              View Details
            </button>
            <button onClick={() => cancelTrip(trip.tripId)} className="danger">
              Cancel Trip
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

// Placeholder functions
const viewTripDetails = (tripId: string) => {
  console.log('View trip details:', tripId);
};

const cancelTrip = (tripId: string) => {
  if (window.confirm('Are you sure you want to cancel this trip?')) {
    console.log('Cancel trip:', tripId);
  }
};
```

---

### 4. React Component - Passenger's Upcoming Rides

```typescript
// components/PassengerUpcomingRides.tsx

import React, { useEffect, useState } from 'react';
import { tripService } from '../services/tripService';
import { PassengerUpcomingRide } from '../types/trips';
import { format, parseISO } from 'date-fns';

export const PassengerUpcomingRides: React.FC = () => {
  const [rides, setRides] = useState<PassengerUpcomingRide[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadUpcomingRides();
  }, []);

  const loadUpcomingRides = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await tripService.getMyUpcomingRides();
      setRides(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load rides');
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateTimeStr: string) => {
    return format(parseISO(dateTimeStr), 'MMM dd, yyyy hh:mm a');
  };

  if (loading) {
    return <div className="loading">Loading your upcoming rides...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>Error: {error}</p>
        <button onClick={loadUpcomingRides}>Retry</button>
      </div>
    );
  }

  if (rides.length === 0) {
    return (
      <div className="empty-state">
        <p>You don't have any upcoming rides.</p>
        <button onClick={() => window.location.href = '/search-rides'}>
          Search for Rides
        </button>
      </div>
    );
  }

  return (
    <div className="upcoming-rides">
      <h2>My Upcoming Rides ({rides.length})</h2>
      
      {rides.map((ride) => (
        <div key={ride.rideId} className="ride-card">
          <div className="ride-header">
            <span className="vehicle">{ride.vehicleNumber}</span>
            <span className={`status ${ride.rideStatus.toLowerCase()}`}>
              {ride.rideStatus}
            </span>
          </div>

          <div className="ride-route">
            <div className="location">
              <span className="label">Pickup:</span>
              <span className="address">{ride.pickupLocation.placeAddress}</span>
            </div>
            <div className="location">
              <span className="label">Dropoff:</span>
              <span className="address">{ride.dropoffLocation.placeAddress}</span>
            </div>
          </div>

          <div className="ride-details">
            <div className="detail-item">
              <span className="icon">🕒</span>
              <span>{formatDateTime(ride.tripStartDateTime)}</span>
            </div>
            <div className="detail-item">
              <span className="icon">📏</span>
              <span>{ride.rideDistanceInKm.toFixed(1)} km</span>
            </div>
            <div className="detail-item">
              <span className="icon">💰</span>
              <span>€{ride.estimatedFare.toFixed(2)}</span>
            </div>
          </div>

          {ride.driverDetails && (
            <div className="driver-info">
              <h4>Driver: {ride.driverDetails.name}</h4>
              <div className="driver-stats">
                <span>⭐ {ride.driverDetails.rating.toFixed(1)}</span>
                <span>🚗 {ride.driverDetails.totalTrips} trips</span>
              </div>
            </div>
          )}

          <div className="ride-actions">
            <button onClick={() => viewRideDetails(ride.rideId)}>
              View Details
            </button>
            <button onClick={() => cancelRide(ride.rideId)} className="danger">
              Cancel Ride
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

// Placeholder functions
const viewRideDetails = (rideId: string) => {
  console.log('View ride details:', rideId);
};

const cancelRide = (rideId: string) => {
  if (window.confirm('Are you sure you want to cancel this ride?')) {
    console.log('Cancel ride:', rideId);
  }
};
```

---

## Implementation Checklist

### Phase 1: Driver's Upcoming Trips ✅
- [x] Create `DriverUpcomingTripDTO`
- [x] Create service methods
- [x] Create controller endpoint
- [x] Add API documentation

### Frontend Tasks:
- [ ] Add TypeScript types
- [ ] Implement API service functions
- [ ] Create Driver's Upcoming Trips UI component
- [ ] Add navigation/routing
- [ ] Style the components
- [ ] Add loading and error states
- [ ] Test with real data

### Phase 2: Passenger's Upcoming Rides 🔄
- [x] Create `PassengerUpcomingRideDTO`
- [x] Create service methods (placeholder)
- [x] Create controller endpoint
- [ ] **Backend:** Implement booking/rider system
- [ ] **Backend:** Create Rider repository queries
- [ ] **Backend:** Link trips with passenger bookings

### Frontend Tasks (After Backend Complete):
- [ ] Implement Passenger's Upcoming Rides UI
- [ ] Test booking flow
- [ ] Add ride cancellation
- [ ] Show driver details

---

## Important Notes

### 1. User ID Extraction
The backend expects `X-User-Id` header. Make sure your API Gateway or auth middleware extracts the user ID from the JWT token and adds it to the request headers.

### 2. Timezone Display
- Backend returns times in ISO-8601 format with timezone
- Use `date-fns` or `moment-timezone` to display in user's local time
- Example: `2025-10-13T20:06:00+02:00` → Display as "Oct 13, 2025 8:06 PM"

### 3. Empty States
- Show meaningful empty states when no trips/rides
- Provide call-to-action buttons (e.g., "Offer a Ride", "Search for Rides")

### 4. Error Handling
- Handle 401 (Unauthorized) → Redirect to login
- Handle 500 (Server Error) → Show retry option
- Show user-friendly error messages

### 5. Passenger Booking System
**Important:** The passenger upcoming rides endpoint is currently a placeholder. It will return an empty array until the booking system is implemented. This requires:
- Creating a `Bookings` or `Riders` collection in MongoDB
- Implementing booking creation when passengers join a trip
- Linking bookings to trips and users

---

## Testing

### Test with cURL

```bash
# Get driver's upcoming trips
curl -X GET "https://api-gateway-yvbz.onrender.com/api/trips/my-trips/upcoming" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-User-Id: driver@example.com"

# Get passenger's upcoming rides
curl -X GET "https://api-gateway-yvbz.onrender.com/api/trips/my-rides/upcoming" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-User-Id: passenger@example.com"
```

---

## Next Steps

1. ✅ **Backend is ready** for driver's upcoming trips
2. 🔄 **Passenger rides** need booking system implementation
3. 📱 **Frontend** can start implementing driver's view
4. 🧪 **Test** with real user data
5. 🎨 **Style** according to your design system

---

## Questions or Issues?

If you encounter any issues or need clarification:
1. Check the API documentation
2. Test with cURL to isolate frontend/backend issues
3. Check browser console for errors
4. Verify JWT token is valid and not expired
5. Ensure `X-User-Id` header is being sent correctly

