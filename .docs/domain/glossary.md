# Domain Glossary

> This document defines the core terminology used throughout the Car Sharing Platform.

| **Term**      | **Definition**                                                             | **Key Point**                                       |
| ------------- | -------------------------------------------------------------------------- | --------------------------------------------------- |
| **User**      | A registered account in the system.                                        | Has a role: **Driver**, **Passenger**, or **Admin** |
| **Driver**    | A user who **creates trips** and is responsible for operating the vehicle. | Publishes trips + manages availability              |
| **Passenger** | A user who **books seats** on trips offered by drivers.                    | Makes bookings and joins rides                      |
| **Admin**     | A user with platform-level administrative permissions.                     | Moderation, audits, system management               |
| **Trip**      | A driver's full published journey from **origin → destination**.           | Can contain multiple passenger ride segments        |
| **Booking**   | A passenger's reservation request for **seats on a trip**.                 | Request/hold/confirmed/cancelled lifecycle          |
| **Ride**      | A passenger's actual **segment** within a trip: **pickup → dropoff**.      | Sub-route; may differ from trip origin/destination  |

---

## Detailed Definitions

### User
A registered account in the system. Every user has a unique identifier and may assume multiple roles. A user can be both a driver and a passenger on different occasions.

**Key Attributes:**
- `userId`: Unique identifier
- `role`: DRIVER, PASSENGER, or ADMIN
- `profile`: Personal information (name, photo, contact)

---

### Driver
A user who creates and manages trips. The driver owns the vehicle, sets the route, determines pricing, and is responsible for operating the vehicle safely.

**Responsibilities:**
- Offer rides with origin, destination, time, seats, and price
- Accept or reject passenger booking requests
- Start and complete the trip
- Receive ratings from passengers

---

### Passenger
A user who searches for available trips and books seats. The passenger specifies pickup and dropoff locations within the driver's route.

**Responsibilities:**
- Search for matching trips
- Request to join trips (create bookings)
- Cancel bookings (subject to rules)
- Rate drivers after completed trips

---

### Admin
A user with elevated permissions for platform management. Admins can moderate content, review disputes, and access system-level analytics.

**Capabilities:**
- View and moderate trips and bookings
- Manage user accounts
- Access audit logs
- Generate platform statistics

---

### Trip
A driver's published journey from origin to destination with specific departure time, available seats, and pricing. A trip may serve multiple passengers, each with their own pickup/dropoff points along the route. Drivers can optionally define intermediate stops (waypoints) along their route to make it easier for passengers to find rides.

**Lifecycle States:**
```
CREATED → AVAILABLE → IN_PROGRESS → COMPLETED
              ↓
          CANCELLED
```

> **Note:** Trips are currently created as `AVAILABLE` directly. The `CREATED` state exists in the state machine but is not actively used - the driver would need to explicitly "publish" a trip to transition from CREATED to AVAILABLE.

**Key Attributes:**
- `tripId`: Unique identifier
- `driverId`: User ID of the driver
- `vehicleNumber`: Driver's registered vehicle
- `sourceAddress` / `destinationAddress`: Trip endpoints
- `stops`: Optional list of intermediate waypoints (in future) (e.g., cities/towns along the route) 
- `totalSeats`: Maximum passengers allowed
- `bookedSeats`: Currently occupied seats
- `tripStartDateTimeUTC`: Departure time in UTC
- `pricePerSeat`: Cost per passenger

---

### Booking
A passenger's reservation for seats on a trip. A booking represents the intent to ride and goes through an approval process.

**Lifecycle States:**
```
REQUESTED → CONFIRMED → IN_PROGRESS → COMPLETED
     ↓          ↓
  REJECTED  CANCELLED
```

**Key Attributes:**
- `bookingId`: Unique identifier
- `passengerId`: User ID of the passenger
- `tripId`: Associated trip
- `pickupLocation` / `dropoffLocation`: Passenger-specific waypoints
- `requestedSeats`: Number of seats requested
- `status`: Current booking state

---

### Ride
A passenger's actual journey segment within a trip. While a trip is the driver's full route, a ride is a passenger's specific portion: pickup → dropoff.

**Key Differences:**

| **Trip** | **Ride** |
|----------|----------|
| Driver's full journey | Passenger's segment |
| Origin → Destination | Pickup → Dropoff |
| Managed by driver | Managed by passenger |
| Contains multiple rides | Belongs to one trip |

**Example:**
- **Trip**: Dar es Salaam → Arusha (500km)
- **Ride 1**: Dodoma → Arusha (Passenger A joins at Dodoma)
- **Ride 2**: Dar es Salaam → Dodoma (Passenger B leaves at Dodoma)

---

## Terminology Note: Ride vs Booking

This codebase uses **two parallel terms** for the passenger's reservation:

| Term | Context | Example |
|------|---------|---------|
| **Ride** | User-facing / API layer | "I booked a ride", `/api/rides/*` endpoints |
| **Booking** | Internal / Entity layer | `Booking.java` entity, `BookingStatus` enum |

### Why Two Terms?

- **Ride** feels natural to end users ("my ride starts at 3pm")
- **Booking** accurately represents the internal entity and state machine

### Usage in Code

| Layer | Preferred Term |
|-------|----------------|
| API Endpoints | `Ride` (e.g., `/api/rides/book`, `/api/rides/cancel`) |
| Controllers | `RideController` |
| DTOs (response) | `PassengerRideResponse`, `OfferRideResponse` |
| Entities | `Booking.java` (internal) |
| Status enums | `BookingStatus` (REQUESTED, CONFIRMED, etc.) |
| Services | Methods use "ride" for user-facing operations |

### Field Mapping

When converting between layers:
- API `rideId` maps to entity `bookingId`
- API `rideStatus` maps to entity field of type `BookingStatus`

---

## DTO Naming Conventions

All DTOs follow consistent naming patterns:

### Request DTOs
```
{Action}{Entity}Request
```
Examples: `OfferRideRequest`, `JoinTripRequest`, `FindRideRequest`, `CancelTripRequest`

### Response DTOs
```
{Entity}Response
{Role}{Entity}Response
```
Examples: `TripResponse`, `BookingResponse`, `DriverTripResponse`, `PassengerRideResponse`

### Special Cases
- Reports: `{ReportType}Report` (e.g., `RevenueReport`, `BookingSummaryReport`)
- Audit: `AuditLogResponse`

### Location Fields

| Level | Fields | Example DTO |
|-------|--------|-------------|
| Trip (driver's full journey) | `sourceAddress` / `destinationAddress` | `TripResponse`, `DriverTripResponse` |
| Booking (passenger's segment) | `pickupLocation` / `dropoffLocation` | `PassengerRideResponse`, `BookingResponse` |

---

## Related Documents

- [User Flows](../product/user-flows.md) - Detailed user journeys
- [API Guide](../technical/_main/api-guide.md) - Endpoint documentation
- [Architecture](../technical/_main/architecture.md) - System design