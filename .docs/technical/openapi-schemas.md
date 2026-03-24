# OpenAPI Schemas and Models

This document provides detailed information about all data models and schemas used in the Car Sharing API.

## Table of Contents

1. [Authentication Schemas](#authentication-schemas)
2. [User Schemas](#user-schemas)
3. [Vehicle Schemas](#vehicle-schemas)
4. [Trip Schemas](#trip-schemas)
5. [Booking Schemas](#booking-schemas)
6. [Common Schemas](#common-schemas)
7. [Enum Definitions](#enum-definitions)

---

## Authentication Schemas

### LoginRequest

User credentials for authentication.

```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string",
      "format": "email",
      "description": "User's email address",
      "example": "john.driver@example.com"
    },
    "password": {
      "type": "string",
      "description": "User's password",
      "example": "SecurePassword123!"
    }
  },
  "required": ["email", "password"]
}
```

### LoginResponse

Successful authentication response.

```json
{
  "type": "object",
  "properties": {
    "token": {
      "type": "string",
      "description": "JWT authentication token",
      "example": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    },
    "email": {
      "type": "string",
      "format": "email",
      "description": "Authenticated user's email",
      "example": "john.driver@example.com"
    },
    "role": {
      "type": "string",
      "enum": ["DRIVER", "PASSENGER"],
      "description": "User's role in the system",
      "example": "DRIVER"
    }
  },
  "required": ["token", "email", "role"]
}
```

### RegisterUserRequest

User registration data.

```json
{
  "type": "object",
  "properties": {
    "firstName": {
      "type": "string",
      "description": "User's first name",
      "example": "John",
      "minLength": 1,
      "maxLength": 50
    },
    "lastName": {
      "type": "string",
      "description": "User's last name",
      "example": "Driver",
      "minLength": 1,
      "maxLength": 50
    },
    "email": {
      "type": "string",
      "format": "email",
      "description": "User's email (unique)",
      "example": "john.driver@example.com"
    },
    "password": {
      "type": "string",
      "description": "User's password (minimum 8 characters)",
      "example": "SecurePassword123!",
      "minLength": 8
    },
    "userType": {
      "type": "string",
      "enum": ["DRIVER", "PASSENGER"],
      "description": "User's role in the system",
      "example": "DRIVER"
    },
    "phoneNumber": {
      "type": "string",
      "description": "Contact phone number (optional)",
      "example": "+49 123 456789"
    },
    "age": {
      "type": "integer",
      "description": "User's age (optional)",
      "example": 32,
      "minimum": 18,
      "maximum": 120
    },
    "vehicleModel": {
      "type": "string",
      "description": "Vehicle model for drivers (optional)",
      "example": "Tesla Model 3"
    },
    "licensePlate": {
      "type": "string",
      "description": "Vehicle license plate for drivers (optional)",
      "example": "HE-XX-1234"
    }
  },
  "required": ["firstName", "lastName", "email", "password", "userType"]
}
```

### SignUpResponse

Response after successful registration.

```json
{
  "type": "object",
  "properties": {
    "token": {
      "type": "string",
      "description": "JWT token for authenticated access",
      "example": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    },
    "email": {
      "type": "string",
      "description": "Registered user's email",
      "example": "john.driver@example.com"
    },
    "role": {
      "type": "string",
      "enum": ["DRIVER", "PASSENGER"],
      "description": "Assigned user role",
      "example": "DRIVER"
    }
  },
  "required": ["token", "email", "role"]
}
```

### ValidateTokenRequest

Request to validate a JWT token.

```json
{
  "type": "object",
  "properties": {
    "token": {
      "type": "string",
      "description": "JWT token to validate",
      "example": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  },
  "required": ["token"]
}
```

### ValidateTokenResponse

Response from token validation.

```json
{
  "type": "object",
  "properties": {
    "isValid": {
      "type": "boolean",
      "description": "Whether the token is valid",
      "example": true
    },
    "email": {
      "type": "string",
      "description": "User's email from token",
      "example": "john.driver@example.com"
    },
    "role": {
      "type": "string",
      "enum": ["DRIVER", "PASSENGER"],
      "description": "User's role from token",
      "example": "DRIVER"
    }
  },
  "required": ["isValid", "email", "role"]
}
```

---

## User Schemas

### UserResponse

User profile information.

```json
{
  "type": "object",
  "properties": {
    "userId": {
      "type": "string",
      "description": "Unique user identifier",
      "example": "user_123"
    },
    "firstName": {
      "type": "string",
      "description": "User's first name",
      "example": "John"
    },
    "lastName": {
      "type": "string",
      "description": "User's last name",
      "example": "Driver"
    },
    "email": {
      "type": "string",
      "format": "email",
      "description": "User's email address",
      "example": "john.driver@example.com"
    },
    "phoneNumber": {
      "type": "string",
      "description": "User's phone number",
      "example": "+49 123 456789"
    },
    "age": {
      "type": "integer",
      "description": "User's age",
      "example": 32
    },
    "role": {
      "type": "string",
      "enum": ["DRIVER", "PASSENGER"],
      "description": "User's role",
      "example": "DRIVER"
    },
    "profileCompletedAt": {
      "type": "string",
      "format": "date-time",
      "description": "ISO 8601 timestamp of profile completion",
      "example": "2024-01-15T10:30:00Z"
    },
    "createdAt": {
      "type": "string",
      "format": "date-time",
      "description": "ISO 8601 timestamp of account creation",
      "example": "2024-01-15T10:00:00Z"
    },
    "updatedAt": {
      "type": "string",
      "format": "date-time",
      "description": "ISO 8601 timestamp of last update",
      "example": "2024-01-15T10:30:00Z"
    }
  },
  "required": ["userId", "firstName", "lastName", "email", "role"]
}
```

### CreateProfileRequest

Request to create or update user profile.

```json
{
  "type": "object",
  "properties": {
    "userId": {
      "type": "string",
      "description": "User ID to create profile for",
      "example": "user_123"
    },
    "firstName": {
      "type": "string",
      "description": "First name",
      "example": "John"
    },
    "lastName": {
      "type": "string",
      "description": "Last name",
      "example": "Driver"
    },
    "phoneNumber": {
      "type": "string",
      "description": "Phone number (optional)",
      "example": "+49 123 456789"
    },
    "age": {
      "type": "integer",
      "description": "Age (optional)",
      "example": 32
    }
  },
  "required": ["userId", "firstName", "lastName"]
}
```

---

## Vehicle Schemas

### RegisterVehicleRequest

Request to register a vehicle.

```json
{
  "type": "object",
  "properties": {
    "userId": {
      "type": "string",
      "description": "Driver's user ID",
      "example": "user_123"
    },
    "vehicleName": {
      "type": "string",
      "description": "Vehicle make and model",
      "example": "Tesla Model 3",
      "minLength": 1,
      "maxLength": 100
    },
    "vehicleNumber": {
      "type": "string",
      "description": "License plate (unique)",
      "example": "HE-XX-1234",
      "pattern": "^[A-Z]{1,3}-[A-Z]{1,2}-[0-9]{1,4}$"
    },
    "vehicleType": {
      "type": "string",
      "enum": ["sedan", "suv", "minivan", "hatchback"],
      "description": "Vehicle category",
      "example": "sedan"
    },
    "vehicleColor": {
      "type": "string",
      "description": "Vehicle color",
      "example": "white",
      "maxLength": 50
    },
    "seatingCapacity": {
      "type": "string",
      "description": "Number of available seats",
      "example": "5",
      "pattern": "^[0-9]{1,2}$"
    }
  },
  "required": ["userId", "vehicleName", "vehicleNumber", "vehicleType", "seatingCapacity"]
}
```

### VehicleResponse

Vehicle information.

```json
{
  "type": "object",
  "properties": {
    "value": {
      "type": "string",
      "description": "Vehicle unique identifier",
      "example": "vehicle_001"
    },
    "text": {
      "type": "string",
      "description": "Display text (model and license plate)",
      "example": "Tesla Model 3 - HE-XX-1234"
    },
    "seatingCapacity": {
      "type": "string",
      "description": "Number of available seats",
      "example": "5"
    }
  },
  "required": ["value", "text", "seatingCapacity"]
}
```

---

## Trip Schemas

### OfferRideRequest

Request to create a trip.

```json
{
  "type": "object",
  "properties": {
    "driverId": {
      "type": "string",
      "description": "Driver's user ID",
      "example": "user_123"
    },
    "vehicleId": {
      "type": "string",
      "description": "Vehicle ID to use for trip",
      "example": "vehicle_001"
    },
    "sourceLatitude": {
      "type": "number",
      "format": "double",
      "description": "Pickup location latitude",
      "example": 50.8154,
      "minimum": -90,
      "maximum": 90
    },
    "sourceLongitude": {
      "type": "number",
      "format": "double",
      "description": "Pickup location longitude",
      "example": 8.6747,
      "minimum": -180,
      "maximum": 180
    },
    "destinationLatitude": {
      "type": "number",
      "format": "double",
      "description": "Dropoff location latitude",
      "example": 50.7265,
      "minimum": -90,
      "maximum": 90
    },
    "destinationLongitude": {
      "type": "number",
      "format": "double",
      "description": "Dropoff location longitude",
      "example": 7.0895,
      "minimum": -180,
      "maximum": 180
    },
    "departureTime": {
      "type": "string",
      "format": "date-time",
      "description": "Trip departure time (ISO 8601)",
      "example": "2024-03-25T08:00:00Z"
    },
    "availableSeats": {
      "type": "integer",
      "description": "Number of seats available for passengers",
      "example": 3,
      "minimum": 1,
      "maximum": 8
    },
    "pricePerSeat": {
      "type": "number",
      "format": "decimal",
      "description": "Price per seat in EUR",
      "example": 15.50,
      "minimum": 0
    },
    "notes": {
      "type": "string",
      "description": "Additional notes about the trip",
      "example": "Morning commute to Frankfurt",
      "maxLength": 500
    }
  },
  "required": [
    "driverId",
    "vehicleId",
    "sourceLatitude",
    "sourceLongitude",
    "destinationLatitude",
    "destinationLongitude",
    "departureTime",
    "availableSeats"
  ]
}
```

### OfferRideResponse

Response after creating a trip.

```json
{
  "type": "object",
  "properties": {
    "tripId": {
      "type": "string",
      "description": "Unique trip identifier",
      "example": "trip_001"
    },
    "driverId": {
      "type": "string",
      "description": "Driver's user ID",
      "example": "user_123"
    },
    "departureTime": {
      "type": "string",
      "format": "date-time",
      "description": "Trip departure time",
      "example": "2024-03-25T08:00:00Z"
    },
    "availableSeats": {
      "type": "integer",
      "description": "Available seats",
      "example": 3
    },
    "status": {
      "type": "string",
      "enum": ["SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"],
      "description": "Trip status",
      "example": "SCHEDULED"
    }
  },
  "required": ["tripId", "driverId", "departureTime", "availableSeats", "status"]
}
```

### Trip

Complete trip information.

```json
{
  "type": "object",
  "properties": {
    "tripId": {
      "type": "string",
      "description": "Trip unique identifier",
      "example": "trip_001"
    },
    "driverId": {
      "type": "string",
      "description": "Driver's user ID",
      "example": "user_123"
    },
    "vehicleId": {
      "type": "string",
      "description": "Vehicle ID",
      "example": "vehicle_001"
    },
    "sourceLocation": {
      "$ref": "#/components/schemas/Location"
    },
    "destinationLocation": {
      "$ref": "#/components/schemas/Location"
    },
    "departureTime": {
      "type": "string",
      "format": "date-time",
      "description": "Departure time",
      "example": "2024-03-25T08:00:00Z"
    },
    "estimatedArrivalTime": {
      "type": "string",
      "format": "date-time",
      "description": "Estimated arrival time",
      "example": "2024-03-25T09:30:00Z"
    },
    "totalSeats": {
      "type": "integer",
      "description": "Total seats in vehicle",
      "example": 5
    },
    "bookedSeats": {
      "type": "integer",
      "description": "Currently booked seats",
      "example": 2
    },
    "availableSeats": {
      "type": "integer",
      "description": "Available seats",
      "example": 3
    },
    "pricePerSeat": {
      "type": "number",
      "format": "decimal",
      "description": "Price per seat in EUR",
      "example": 15.50
    },
    "status": {
      "type": "string",
      "enum": ["SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"],
      "description": "Trip status",
      "example": "SCHEDULED"
    },
    "passengers": {
      "type": "array",
      "description": "Booked passengers",
      "items": {
        "type": "object",
        "properties": {
          "passengerId": {
            "type": "string",
            "example": "user_456"
          },
          "firstName": {
            "type": "string",
            "example": "Jane"
          },
          "lastName": {
            "type": "string",
            "example": "Passenger"
          }
        }
      }
    },
    "createdAt": {
      "type": "string",
      "format": "date-time",
      "description": "Trip creation time",
      "example": "2024-03-24T20:00:00Z"
    }
  },
  "required": [
    "tripId",
    "driverId",
    "vehicleId",
    "sourceLocation",
    "destinationLocation",
    "departureTime",
    "availableSeats",
    "status"
  ]
}
```

### DriverTripResponse

Trip information from driver's perspective.

```json
{
  "type": "object",
  "properties": {
    "tripId": {
      "type": "string",
      "example": "trip_001"
    },
    "status": {
      "type": "string",
      "enum": ["SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"],
      "example": "SCHEDULED"
    },
    "departureTime": {
      "type": "string",
      "format": "date-time",
      "example": "2024-03-25T08:00:00Z"
    },
    "destinationLocation": {
      "$ref": "#/components/schemas/Location"
    },
    "currentPassengers": {
      "type": "integer",
      "example": 2
    },
    "availableSeats": {
      "type": "integer",
      "example": 1
    }
  },
  "required": ["tripId", "status", "departureTime", "destinationLocation", "currentPassengers", "availableSeats"]
}
```

### CancelTripRequest

Request to cancel a trip.

```json
{
  "type": "object",
  "properties": {
    "tripId": {
      "type": "string",
      "description": "Trip to cancel",
      "example": "trip_001"
    },
    "userId": {
      "type": "string",
      "description": "User cancelling the trip",
      "example": "user_123"
    },
    "reason": {
      "type": "string",
      "description": "Cancellation reason (optional)",
      "example": "Change of plans",
      "maxLength": 500
    },
    "rideId": {
      "type": "string",
      "description": "Ride/booking ID for passengers",
      "example": "ride_001"
    }
  },
  "required": ["tripId", "userId"]
}
```

---

## Booking Schemas

### JoinTripRequest

Request to join a trip as passenger.

```json
{
  "type": "object",
  "properties": {
    "passengerId": {
      "type": "string",
      "description": "Passenger's user ID",
      "example": "user_456"
    },
    "tripId": {
      "type": "string",
      "description": "Trip to join",
      "example": "trip_001"
    },
    "numberOfSeats": {
      "type": "integer",
      "description": "Number of seats to book",
      "example": 2,
      "minimum": 1,
      "maximum": 8
    },
    "specialRequests": {
      "type": "string",
      "description": "Special requests or notes",
      "example": "I have a large suitcase",
      "maxLength": 500
    }
  },
  "required": ["passengerId", "tripId", "numberOfSeats"]
}
```

### PassengerRideResponse

Booking information from passenger's perspective.

```json
{
  "type": "object",
  "properties": {
    "rideId": {
      "type": "string",
      "description": "Booking unique identifier",
      "example": "ride_001"
    },
    "tripId": {
      "type": "string",
      "description": "Associated trip",
      "example": "trip_001"
    },
    "driverId": {
      "type": "string",
      "description": "Driver's user ID",
      "example": "user_123"
    },
    "driverName": {
      "type": "string",
      "description": "Driver's full name",
      "example": "John Driver"
    },
    "vehicleInfo": {
      "type": "string",
      "description": "Vehicle model and license plate",
      "example": "Tesla Model 3 - HE-XX-1234"
    },
    "departureTime": {
      "type": "string",
      "format": "date-time",
      "example": "2024-03-25T08:00:00Z"
    },
    "destinationLocation": {
      "$ref": "#/components/schemas/Location"
    },
    "seatsBooked": {
      "type": "integer",
      "example": 2
    },
    "totalPrice": {
      "type": "number",
      "format": "decimal",
      "description": "Total price in EUR",
      "example": 31.00
    },
    "status": {
      "type": "string",
      "enum": ["CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED"],
      "example": "CONFIRMED"
    }
  },
  "required": [
    "rideId",
    "tripId",
    "driverId",
    "departureTime",
    "destinationLocation",
    "seatsBooked",
    "totalPrice",
    "status"
  ]
}
```

---

## Common Schemas

### Location

Geographic coordinates.

```json
{
  "type": "object",
  "properties": {
    "latitude": {
      "type": "number",
      "format": "double",
      "description": "Latitude coordinate",
      "example": 50.8154,
      "minimum": -90,
      "maximum": 90
    },
    "longitude": {
      "type": "number",
      "format": "double",
      "description": "Longitude coordinate",
      "example": 8.6747,
      "minimum": -180,
      "maximum": 180
    },
    "address": {
      "type": "string",
      "description": "Human-readable address (optional)",
      "example": "Marburg, Germany"
    }
  },
  "required": ["latitude", "longitude"]
}
```

### ApiResponse

Standard API response wrapper.

```json
{
  "type": "object",
  "properties": {
    "success": {
      "type": "boolean",
      "description": "Whether request succeeded",
      "example": true
    },
    "data": {
      "type": "object",
      "description": "Response data (varies by endpoint)"
    },
    "error": {
      "type": "string",
      "description": "Error code (if failed)",
      "example": "VALIDATION_ERROR"
    },
    "message": {
      "type": "string",
      "description": "Success or error message",
      "example": "Operation completed successfully"
    }
  },
  "required": ["success", "message"]
}
```

### PaginatedResponse

Standard paginated response.

```json
{
  "type": "object",
  "properties": {
    "content": {
      "type": "array",
      "description": "Items on current page",
      "items": {}
    },
    "page": {
      "type": "integer",
      "description": "Current page number (0-indexed)",
      "example": 0
    },
    "size": {
      "type": "integer",
      "description": "Items per page",
      "example": 20
    },
    "totalElements": {
      "type": "integer",
      "description": "Total number of items",
      "example": 150
    },
    "totalPages": {
      "type": "integer",
      "description": "Total number of pages",
      "example": 8
    },
    "last": {
      "type": "boolean",
      "description": "Is this the last page",
      "example": false
    }
  },
  "required": ["content", "page", "size", "totalElements", "totalPages", "last"]
}
```

---

## Enum Definitions

### UserType

```typescript
enum UserType {
  DRIVER = "DRIVER",
  PASSENGER = "PASSENGER"
}
```

### TripStatus

```typescript
enum TripStatus {
  SCHEDULED = "SCHEDULED",
  IN_PROGRESS = "IN_PROGRESS",
  COMPLETED = "COMPLETED",
  CANCELLED = "CANCELLED"
}
```

### RideStatus

```typescript
enum RideStatus {
  CONFIRMED = "CONFIRMED",
  IN_PROGRESS = "IN_PROGRESS",
  COMPLETED = "COMPLETED",
  CANCELLED = "CANCELLED"
}
```

### VehicleType

```typescript
enum VehicleType {
  SEDAN = "sedan",
  SUV = "suv",
  MINIVAN = "minivan",
  HATCHBACK = "hatchback"
}
```

---

## Validation Rules

### Email Format
- Must be valid email format: `user@example.com`
- Must be unique in the system
- Case-insensitive

### Password Requirements
- Minimum 8 characters
- Recommended: Mix of uppercase, lowercase, numbers, and special characters

### License Plate Format
- German format: `XX-XX-1234`
- Pattern: `^[A-Z]{1,3}-[A-Z]{1,2}-[0-9]{1,4}$`
- Must be unique in the system

### Coordinates (Latitude/Longitude)
- Latitude: -90 to 90
- Longitude: -180 to 180
- Precision: Up to 6 decimal places

### Timestamps
- All timestamps use ISO 8601 format: `YYYY-MM-DDTHH:MM:SSZ`
- All times in UTC

---

## Field Constraints

| Field | Type | Min | Max | Required |
|-------|------|-----|-----|----------|
| firstName | string | 1 | 50 | Yes |
| lastName | string | 1 | 50 | Yes |
| email | string (email) | 5 | 255 | Yes |
| password | string | 8 | 255 | Yes |
| phoneNumber | string | 10 | 20 | No |
| age | integer | 18 | 120 | No |
| vehicleModel | string | 1 | 100 | No |
| availableSeats | integer | 1 | 8 | Yes |
| pricePerSeat | decimal | 0.0 | 999.99 | No |
| notes | string | 0 | 500 | No |
| specialRequests | string | 0 | 500 | No |

---

Last Updated: March 24, 2024
