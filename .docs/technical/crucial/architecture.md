# Car Sharing Microservices Architecture

This project has been transformed from a monolithic architecture to a microservices architecture using Spring Boot and Spring Cloud.

- [Operations & Setup Guide](operations.md)

## Architecture Overview

The system is built using a microservices architecture, leveraging Spring Boot and Spring Cloud. It uses OSRM for route and distance calculations, Firebase for real-time notifications, and MongoDB for persistence. Authentication is handled internally via a dedicated JWT-based Auth Service.

### Technology Stack

- **Java**: Java 21
- **Framework**: Spring Boot with Spring MVC, Spring Cloud (Kubernetes Discovery, API Gateway, Feign)
- **Database**:
  - MongoDB Atlas (for persistent storage)
  - Firebase Realtime Database (for real-time notifications ONLY)
- **APIs**:
  - OSRM (for directions and distance calculations)
- **Authentication**:
  - Internal JWT-based Identity Provider (Auth Service)
- **Other Libraries**:
  - Lombok (for reducing boilerplate code)
  - Spring Data Mongo (for MongoDB integration)
  - Jakarta EE

### Core Services

All services run on **port 8080** inside their containers and are routed through the API Gateway by path prefix.

1. **API Gateway** (`/`) - Single entry point for all client requests; validates JWT, propagates `X-User-Id`/`X-User-Role` headers
2. **Auth Service** (`/auth-service`) - User registration, login, JWT issuance and validation, password reset/change
3. **User Service** (`/user-service`) - User profiles and vehicle registration/management
4. **Trip Service** (`/trip-service`) - Trip creation (driver), trip search, ride booking and cancellation (passenger), booking accept/reject (driver)
5. **Review Service** (`/review-service`) - Mutual reviews and ratings after completed trips
6. **Notification Service** (`/notification-service`) - Real-time notifications via WebSocket and MongoDB persistence

### Development Notes

### Security

The system uses a decentralized security model managed at the API Gateway level:
- **Authentication**: JWT-based authentication using an internal `auth-service`. Firebase is NOT used for authentication.
- **Identity Propagation**: The API Gateway validates incoming JWTs and propagates user identity to downstream services via `X-User-Id` and `X-User-Role` headers.
- **Authorization**: Role-Based Access Control (RBAC) is enforced at the service level based on these headers.
- **CORS**: Managed at the Kubernetes Ingress level.

### Supporting Infrastructure

- **MongoDB** - Distributed across services with separate databases
- **GitOps** - Manual deployment workflow using `kubectl` and `kustomize`

## Service Responsibilities

### Auth Service

- User registration and JWT issuance (`POST /api/auth/signup`)
- Login (`POST /api/auth/login`)
- Token validation (`POST /api/auth/validate`) — called internally by API Gateway
- Password reset and change (`POST /api/auth/reset-password`, `PUT /api/auth/change-password`)

### User Service

- User profile creation and retrieval (`/api/users`)
- Vehicle registration and lookup (`/api/vehicles`) — max 5 vehicles per driver
- Admin endpoints for drivers and passengers (`/api/admin`)

### Trip Service

**Driver (trips):**
- Create trip offer (`POST /api/trips/offer`)
- Cancel trip (`POST /api/trips/cancel`)
- Active trips and history (`GET /api/trips/active/driver/{driverId}`, `/history/driver/{driverId}`)

**Passenger (rides):**
- Book a ride (`POST /api/rides/book`)
- Cancel a ride (`POST /api/rides/cancel`)
- Active rides and history (`GET /api/rides/active/passenger/{passengerId}`, `/history/passenger/{passengerId}`)

**Driver booking management:**
- Accept/reject booking requests (`POST /api/bookings/accept`, `/api/bookings/reject`)

**Trip search (all users):**
- Near source (`GET /api/trips/search/near-source`)
- Near destination (`GET /api/trips/search/near-destination`)
- Matching route with filters (`GET /api/trips/search/matching-route`)
- Route geometry and distances via OSRM integration

### Review Service

- Submit review for driver (by passenger) — `POST /api/reviews/driver`
- Submit review for passenger (by driver) — `POST /api/reviews/passenger`
- Retrieve reviews and average ratings for drivers and passengers
- Validates trip/booking completion status via Trip Service before allowing submission

### Notification Service

- Create and persist notifications (`POST /api/notifications`)
- Paginated list per recipient (`GET /api/notifications/{recipientId}`)
- Mark as read — single or all (`PATCH /api/notifications/{notificationId}/read`, `/read-all`)
- Notification stats (`GET /api/notifications/{recipientId}/stats`)
- Real-time delivery via WebSocket

## ⚙️ Technical Capabilities

### Timezone Handling

The system is designed for global use. All trip times are stored in **UTC** in the database. The original timezone (e.g., "Africa/Dar_es_Salaam") is also stored. When searching or viewing trips, the system can convert these times to the user's local timezone, ensuring consistency regardless of location.

### Geospatial Matching

We leverage MongoDB's **2dsphere indexes** for high-performance location-based searches. This allows the system to:

- Find rides starting near a specific point.
- Match riders with trips that intersect their requested route.
- Perform efficient bounding-box queries for map-based discovery.

### Route Integration (OSRM)

The Trip Service integrates with the **OpenStreetMap Routing Machine (OSRM)**. For every offered trip, the system:

- Fetches the optimal driving route.
- Stores the complete route geometry as a GeoJSON LineString.
- Provides accurate distance and duration estimates.

## Database Strategy

The application uses **MongoDB Atlas** for persistent storage. All services connect via the `MONGODB_URI` environment variable. Collections are organized by domain:

- User and vehicle collections (User Service)
- Trip, ride, and booking collections (Trip Service)
- Review collections (Review Service)
- Notification collections (Notification Service)

> [!NOTE]
> While the current implementation uses a shared database, the microservices architecture allows for future migration to separate databases per service if needed for better isolation and scalability.

## Service Dependencies

The Review Service depends on the Trip Service for validating trip completion status before allowing reviews to be submitted. Only participants of completed trips can leave reviews for one another.

## Development Guidelines

1. **Service Communication**: Use Feign clients for inter-service communication.
2. **Data Consistency**: Implement eventual consistency patterns.
3. **Error Handling**: Implement circuit breakers and retry mechanisms.
4. **Logging**: Use distributed tracing for request correlation.
5. **Security**: Implement service-to-service authentication.
