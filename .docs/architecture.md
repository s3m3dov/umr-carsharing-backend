# Car Sharing Microservices Architecture

This project has been transformed from a monolithic architecture to a microservices architecture using Spring Boot and Spring Cloud.

- [Running Guide](running.md)

## Architecture Overview

The system is built using a microservices architecture, leveraging Spring Boot and Spring Cloud. It uses Google Maps API for location services, Firebase for real-time data, and MongoDB for persistence.

### Technology Stack

- **Java**: Java 21
- **Framework**: Spring Boot with Spring MVC, Spring Cloud (Eureka, API Gateway, Feign)
- **Database**:
  - MongoDB Atlas (for persistent storage)
  - Firebase Realtime Database (for real-time data)
- **APIs**:
  - Google Maps API (for directions and distance calculations)
- **Other Libraries**:
  - Lombok (for reducing boilerplate code)
  - Spring Data Mongo (for MongoDB integration)
  - Jakarta EE

### Core Services

1. **Eureka Server** (Port 8761) - Service Discovery
2. **API Gateway** (Port 8080) - Single entry point for all client requests
3. **User Service** (Port 8080) - User management and authentication
4. **Vehicle Service** (Port 8080) - Vehicle registration and management
5. **Trip Service** (Port 8080) - Trip creation and management
6. **Ride Service** (Port 8080) - Ride finding and joining
7. **Notification Service** (Port 8080) - Real-time notifications and WebSocket

### Development Notes

- CORS is managed at the **Kubernetes Ingress level**, keeping the application code environment-agnostic.
- The application handles complex distance calculations for matching rides.
- Each microservice has its own MongoDB database for strict data isolation.

### Supporting Infrastructure

- **MongoDB** - Distributed across services with separate databases
- **Docker Compose** - Container orchestration for local development

## Service Responsibilities

### User Service

- User registration and authentication
- User profile management
- User data persistence

### Vehicle Service

- Vehicle registration
- Vehicle information management
- User vehicle associations

### Trip Service

- Trip creation (offer rides)
- Trip management
- My rides functionality (upcoming/history)
- Trip cancellation

### Ride Service

- Find available rides
- Join trip requests
- Ride matching algorithms
- Integration with Google Maps API

### Notification Service

- WebSocket connections for real-time updates
- Firebase integration for push notifications
- Event-driven notifications between services

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

The application uses **MongoDB Atlas** for persistent storage with a shared database approach. All services connect to the same MongoDB instance (`carsharingDB`) using the `MONGODB_URI` environment variable. Collections are organized by domain:

- User-related collections (User Service)
- Vehicle-related collections (Vehicle Service)
- Trip-related collections (Trip Service)
- Ride-related collections (Ride Service)

**Firebase Realtime Database** is used for real-time notifications and live updates.

> [!NOTE]
> While the current implementation uses a shared database, the microservices architecture allows for future migration to separate databases per service if needed for better isolation and scalability.

## Development Guidelines

1. **Service Communication**: Use Feign clients for inter-service communication.
2. **Data Consistency**: Implement eventual consistency patterns.
3. **Error Handling**: Implement circuit breakers and retry mechanisms.
4. **Logging**: Use distributed tracing for request correlation.
5. **Security**: Implement service-to-service authentication.
