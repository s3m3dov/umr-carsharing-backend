# 🚗 Car Sharing Platform – User Flows

> **Scope:** Core user journeys for a BlaBlaCar-like system  
> **Actors:** Guest · Passenger · Driver  
> **Assumption:** User is authenticated unless stated otherwise

---

## 👤 User Roles

| Role | Description |
|----|------------|
| Guest | Unauthenticated user |
| Passenger | User searching & booking rides |
| Driver | User offering rides |
| Admin | (Future) Moderation & analytics |

---

## 🔍 1. Search Ride (Passenger)

### Goal
Find suitable trips based on route, time, and preferences.

### Flow
1. Passenger enters:
   - Source location
   - Destination location
   - Date & time window
   - Seats needed
2. System performs:
   - Geospatial search (2dsphere)
   - Route overlap matching
   - Time window filtering
3. System ranks results by:
   - Rating
   - Price
   - Detour length
4. Passenger views available trips

### Backend Services
- API Gateway
- Trip Service
- OSRM (routing)

### States
- Trip must be `CREATED` or `CONFIRMED`
- Available seats > 0

---

## 🚘 2. Offer Ride (Driver)

### Goal
Create a trip and make it discoverable.

### Flow
1. Driver provides:
   - Start & destination
   - Date & time
   - Available seats
   - Price per seat
2. System:
   - Calculates route via OSRM
   - Stores route geometry
   - Sets trip state to `CREATED`
3. Trip becomes searchable

### Backend Services
- Trip Service
- Vehicle Service
- OSRM

### States
- Initial state: `CREATED`

---

## 📥 3. Request to Join Ride (Passenger)

### Goal
Request a seat on a trip.

### Flow
1. Passenger selects a trip
2. Passenger sends join request
3. System:
   - Validates seat availability
   - Creates booking with state `REQUESTED`
4. Driver is notified

### Backend Services
- Trip Service
- Notification Service

### Booking State
`REQUESTED`

---

## ✅ 4. Accept / Reject Booking (Driver)

### Goal
Control who joins the trip.

### Flow
1. Driver reviews booking request
2. Driver accepts or rejects
3. System:
   - Updates booking state
   - Adjusts available seats (atomic)
4. Passenger is notified

### Booking States
```

REQUESTED → ACCEPTED
REQUESTED → REJECTED

```

---

## ❌ 5. Cancel Booking or Trip

### Passenger Cancels Booking
1. Passenger cancels booking
2. System:
   - Validates cancellation rules
   - Updates booking state
   - Frees seat
3. Driver notified

### Driver Cancels Trip
1. Driver cancels trip
2. System:
   - Updates trip state to `CANCELLED`
   - Cancels all bookings
3. All passengers notified

### States
```

BOOKING: ACCEPTED → CANCELLED
TRIP: CREATED / CONFIRMED → CANCELLED

```

---

## 🚦 6. Trip Execution

### Goal
Track trip lifecycle.

### Flow
1. Driver starts trip → `IN_PROGRESS`
2. Trip runs
3. Driver completes trip → `COMPLETED`

### Backend Services
- Trip Service

---

## ⭐ 7. Leave Review (Passenger & Driver)

### Goal
Build trust & reputation.

### Flow
1. Trip is `COMPLETED`
2. Passenger reviews driver
3. Driver reviews passenger
4. System:
   - Validates participation
   - Saves review
   - Updates aggregated rating

### Rules
- One review per trip per user
- Only real participants
- Rating range: 1–5

### Backend Services
- Review Service
- Trip Service
- Notification Service

---

## 📊 8. View User Ratings

### Goal
Help users choose reliable drivers/passengers.

### Flow
1. User views profile
2. System returns:
   - Average rating
   - Total reviews
   - Recent comments

### Backend Services
- Review Service
- User Service

---

## 🔔 9. Notifications (Cross-Cutting)

### Triggered Events
- Booking requested
- Booking accepted/rejected
- Trip cancelled
- Trip starting soon
- Review received

### Delivery
- Firebase Realtime Database
- Push notifications (future)

---

## 🔄 State Summary

### Trip States
```

CREATED → CONFIRMED → IN_PROGRESS → COMPLETED → CANCELLED

```

### Booking States
```
REQUESTED → ACCEPTED → CANCELLED → REJECTED

```

---

## 🧠 Notes
- All flows are event-driven internally
- Authorization enforced per role & ownership
