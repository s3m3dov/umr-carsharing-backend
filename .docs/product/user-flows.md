# Car Sharing Platform – User Flows

> **Scope:** Core user journeys for a BlaBlaCar-like system
> **Actors:** Guest · Passenger · Driver
> **Assumption:** User is authenticated unless stated otherwise

---

## User Roles

| Role | Description |
|----|------------|
| Guest | Unauthenticated user |
| Passenger | User searching & booking rides |
| Driver | User offering rides |
| Admin | Moderation & analytics |

---

## 1. Search Ride (Passenger)

### Goal
Find suitable trips based on route, time, and preferences.

### Flow
1. Passenger enters source location, destination, date/time window, seats needed
2. System performs geospatial search (2dsphere), route overlap matching, time window filtering
3. Passenger views available trips

### Backend Services
- API Gateway → Trip Service → OSRM (routing)

### States
- Trip must be `AVAILABLE`
- Available seats > 0

---

## 2. Offer Ride (Driver)

### Goal
Create a trip and make it discoverable.

### Flow
1. Driver provides start & destination, date & time, available seats, price per seat
2. System calculates route via OSRM, stores route geometry, sets trip state to `CREATED`
3. Trip becomes searchable (state transitions to `AVAILABLE`)

### Backend Services
- Trip Service, OSRM

### States
- Initial: `CREATED` → `AVAILABLE`

---

## 3. Book a Ride (Passenger)

Two booking modes exist:

### 3a. Auto-Confirm (`POST /api/rides/book`)
1. Passenger selects trip and books
2. System validates seat availability
3. Booking created immediately as `CONFIRMED` — no driver action needed
4. Driver is notified

### 3b. Driver-Accept (`POST /api/rides/book/new`)
1. Passenger selects trip and requests a seat
2. Booking created as `REQUESTED`
3. Driver is notified and must explicitly accept or reject

### Backend Services
- Trip Service, Notification Service

### Booking States
```
Auto-confirm:    (new) → CONFIRMED
Driver-accept:   (new) → REQUESTED
```

---

## 4. Accept / Reject Booking (Driver) — driver-accept flow only

### Goal
Control who joins the trip.

### Flow
1. Driver reviews booking request in `REQUESTED` state
2. Driver accepts → booking moves to `CONFIRMED`, passenger notified
3. Driver rejects → booking moves to `REJECTED`, seat freed, passenger notified

### Booking States
```
REQUESTED → CONFIRMED
REQUESTED → REJECTED
```

---

## 5. Cancel Booking or Trip

### Passenger Cancels Booking
1. Passenger cancels booking (any active state)
2. System updates booking to `CANCELLED`, frees the seat
3. Driver notified

### Driver Cancels Trip
1. Driver cancels trip
2. System sets trip to `CANCELLED`, bulk-cancels all active bookings
3. All passengers notified

### States
```
Booking: REQUESTED | CONFIRMED → CANCELLED
Trip:    CREATED | AVAILABLE | IN_PROGRESS → CANCELLED
```

---

## 6. Trip Execution

### Goal
Track the live trip lifecycle from start to full completion.

### 6a. Start Trip (driver)
> **Note:** A "start trip" endpoint is not yet implemented. This transition (`AVAILABLE → IN_PROGRESS`) is planned.

1. Driver marks trip as started
2. Trip → `IN_PROGRESS`
3. All `CONFIRMED` bookings → `IN_PROGRESS`

### 6b. Complete a Passenger's Ride (driver drops off a passenger mid-route)

A **ride** is a passenger's segment of the trip (their own pickup → dropoff points). The driver confirms each passenger's drop-off individually while the trip continues.

**Endpoint:** `POST /api/bookings/{bookingId}/complete?driverId={driverId}`

1. Driver confirms passenger has been dropped off at their stop
2. Booking → `COMPLETED`
3. Passenger notified (`RIDE_COMPLETED`)
4. Trip continues — other passengers are unaffected

### 6c. Complete the Full Trip (driver reaches final destination)

**Endpoint:** `POST /api/trips/{tripId}/complete?driverId={driverId}`

1. Driver ends the trip at the final destination
2. Any remaining active bookings (`CONFIRMED` / `IN_PROGRESS`) are auto-completed
3. Trip → `COMPLETED`
4. All auto-completed passengers notified (`TRIP_COMPLETED`)

### Backend Services
- Trip Service, Notification Service

### State Transitions (full picture)
```
Trip:    IN_PROGRESS → COMPLETED
Booking: CONFIRMED | IN_PROGRESS → COMPLETED  (via ride or trip completion)
```

---

## 7. Leave Review (Passenger & Driver)

### Goal
Build trust & reputation.

### Flow
1. Trip and booking are both `COMPLETED`
2. Passenger reviews driver; driver reviews passenger
3. System validates participation, saves review, updates aggregated rating

### Rules
- One review per booking per user
- Only actual participants of the booking can review
- Rating range: 1–5

### Backend Services
- Review Service, Trip Service, Notification Service

---

## 8. View User Ratings

### Goal
Help users choose reliable drivers/passengers.

### Flow
1. User views profile
2. System returns average rating, total reviews, recent comments

### Backend Services
- Review Service, User Service

---

## 9. Notifications (Cross-Cutting)

### Triggered Events

| Event | Recipient | Type |
|---|---|---|
| Booking requested | Driver | `BOOKING_REQUESTED` |
| Booking confirmed (auto or accepted) | Driver | `BOOKING_CONFIRMED` |
| Booking cancelled by passenger | Driver | `BOOKING_CANCELLED_BY_PASSENGER` |
| Passenger dropped off | Passenger | `RIDE_COMPLETED` |
| Trip ended (auto-complete) | Passenger | `TRIP_COMPLETED` |

### Delivery
- WebSocket (Notification Service)

---

## State Summary

### Trip States
```
CREATED → AVAILABLE → IN_PROGRESS → COMPLETED
                    ↘             ↘
                  CANCELLED     CANCELLED
```

### Booking States
```
REQUESTED → CONFIRMED → IN_PROGRESS → COMPLETED
          ↘           ↘             ↘
        REJECTED    CANCELLED     CANCELLED (if trip cancelled)
```

> **Key distinction:** A *trip* belongs to the driver (full route). A *ride* (booking) belongs to the passenger (their segment). Each passenger can be dropped off at their own stop during the trip — their ride completes independently before the trip ends.

---

## Known Gaps / Planned

| Feature | Status |
|---|---|
| Start trip (`AVAILABLE → IN_PROGRESS`) | ⏳ Not yet implemented |
| Payment integration | ⏳ Planned |
| Push notifications (Firebase) | ⏳ Planned |
