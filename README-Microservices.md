# Car Sharing Microservices Architecture

This project has been transformed from a monolithic architecture to a microservices architecture using Spring Boot and Spring Cloud.

## Architecture Overview

The application is now divided into the following microservices:

### Core Services
1. **Eureka Server** (Port 8761) - Service Discovery
2. **API Gateway** (Port 8080) - Single entry point for all client requests
3. **User Service** (Port 8081) - User management and authentication
4. **Vehicle Service** (Port 8082) - Vehicle registration and management
5. **Trip Service** (Port 8083) - Trip creation and management
6. **Ride Service** (Port 8084) - Ride finding and joining
7. **Notification Service** (Port 8085) - Real-time notifications and WebSocket

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

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker and Docker Compose
- MongoDB (or use Docker Compose)

### Environment Variables
Create a `.env` file in the root directory:

```env
GOOGLE_API_KEY=your-google-maps-api-key
FIREBASE_CONFIG_PATH=/path/to/firebase-sdk.json
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
```

### Running with Docker Compose

1. Build all services:
```bash
mvn clean package -DskipTests
```

2. Start all services:
```bash
docker-compose up -d
```

3. Check service health:
```bash
docker-compose ps
```

### Running Locally (Development)

1. Start Eureka Server:
```bash
cd eureka-server
mvn spring-boot:run
```

2. Start other services in order:
```bash
# Terminal 2
cd api-gateway
mvn spring-boot:run

# Terminal 3
cd users-service
mvn spring-boot:run

# Terminal 4
cd vehicle-service
mvn spring-boot:run

# Terminal 5
cd trip-service
mvn spring-boot:run

# Terminal 6
cd ride-service
mvn spring-boot:run

# Terminal 7
cd notification-service
mvn spring-boot:run
```

## API Endpoints

All requests now go through the API Gateway at `http://localhost:8080`

### User Management
- `POST /api/users/signup` - Register new users
- `POST /api/users/login` - User login
- `GET /api/users/{userId}` - Get users profile

### Vehicle Management
- `GET /api/vehicles/{userId}` - Get users vehicles
- `POST /api/vehicles/register` - Register new vehicle

### Trip Management
- `POST /api/ride/create-trip` - Create new trip
- `POST /api/myrides/upcoming` - Get upcoming rides
- `POST /api/myrides/history` - Get ride history
- `POST /api/myrides/cancel` - Cancel ride

### Ride Management
- `POST /api/rides/find-ride` - Find available rides
- `POST /api/rides/join-trip` - Join a trip

## Service Discovery

All services register with Eureka Server. You can view registered services at:
`http://localhost:8761`

## Monitoring

Each service exposes actuator endpoints for health checks:
- `http://localhost:808x/actuator/health`
- `http://localhost:808x/actuator/info`

## Database Strategy

Each microservice has its own MongoDB database:
- `carsharing_users` - User Service
- `carsharing_vehicles` - Vehicle Service  
- `carsharing_trips` - Trip Service
- `carsharing_rides` - Ride Service

## Benefits of Microservices Architecture

1. **Scalability** - Each service can be scaled independently
2. **Technology Diversity** - Different services can use different technologies
3. **Fault Isolation** - Failure in one service doesn't bring down the entire system
4. **Team Independence** - Different teams can work on different services
5. **Deployment Flexibility** - Services can be deployed independently

## Development Guidelines

1. **Service Communication** - Use Feign clients for inter-service communication
2. **Data Consistency** - Implement eventual consistency patterns
3. **Error Handling** - Implement circuit breakers and retry mechanisms
4. **Logging** - Use distributed tracing for request correlation
5. **Security** - Implement service-to-service authentication

## Next Steps

1. Implement remaining controllers and services in each microservice
2. Add circuit breakers using Hystrix or Resilience4j
3. Implement distributed tracing with Sleuth and Zipkin
4. Add comprehensive logging and monitoring
5. Implement API versioning strategy
6. Add integration tests for service interactions