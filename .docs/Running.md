# Running the Car Sharing Microservices

## Quick Start Guide

### Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB (running locally or use Docker)

### Option 1: Run Individual Services (Recommended for Development)

Start services in separate terminals in this order:

#### Terminal 1 - Eureka Server (Service Discovery)

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/eureka-server
mvn spring-boot:run
```

Wait until you see: `Started EurekaServerApplication`
Access: <http://localhost:8761>

#### Terminal 2 - User Service

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/user-service
mvn spring-boot:run
```

Swagger UI: <http://localhost:8081/swagger-ui.html>

#### Terminal 3 - Vehicle Service

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/vehicle-service
mvn spring-boot:run
```

Swagger UI: <http://localhost:8082/swagger-ui.html>

#### Terminal 4 - Trip Service

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/trip-service
mvn spring-boot:run
```

Swagger UI: <http://localhost:8083/swagger-ui.html>

#### Terminal 5 - Ride Service

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/ride-service
mvn spring-boot:run
```

Swagger UI: <http://localhost:8084/swagger-ui.html>

#### Terminal 6 - Notification Service

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/notification-service
mvn spring-boot:run
```

Swagger UI: <http://localhost:8085/swagger-ui.html>

#### Terminal 7 - API Gateway (Aggregated Documentation)

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/api-gateway
mvn spring-boot:run
```

**🎯 Aggregated Swagger UI: <http://localhost:8080/swagger-ui.html>**

---

### Option 2: Run with Docker Compose (All Services at Once)

```bash
cd /Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend

# Build all services
mvn clean package -DskipTests

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

---

## Accessing Swagger Documentation

### 🌟 Best Option: Aggregated View

**URL:** <http://localhost:8080/swagger-ui.html>

- Use the dropdown at the top to switch between services
- All endpoints accessible through the gateway
- Production-like routing

### Individual Service Views

- User Service: <http://localhost:8081/swagger-ui.html>
- Vehicle Service: <http://localhost:8082/swagger-ui.html>
- Trip Service: <http://localhost:8083/swagger-ui.html>
- Ride Service: <http://localhost:8084/swagger-ui.html>
- Notification Service: <http://localhost:8085/swagger-ui.html>

### OpenAPI JSON Specs

- Individual: `http://localhost:808X/v3/api-docs`
- Via Gateway: `http://localhost:8080/v3/api-docs?group=user-service`

---

## Testing the API

### Using Swagger UI (Interactive)

1. Open <http://localhost:8080/swagger-ui.html>
2. Select a service from the dropdown
3. Click on an endpoint to expand it
4. Click "Try it out"
5. Fill in parameters
6. Click "Execute"
7. See the response!

### Example: Register a User

1. Go to <http://localhost:8081/swagger-ui.html>
2. Find `POST /api/users/signup`
3. Click "Try it out"
4. Enter request body:

```json
{
  "userId": "test-user-1",
  "requestContent": {
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "password": "password123"
  }
}
```

5. Click "Execute"
6. See the response!

---

## Monitoring Services

### Eureka Dashboard
<http://localhost:8761>

Shows all registered services and their health status.

### Health Checks

- User Service: <http://localhost:8081/actuator/health>
- Vehicle Service: <http://localhost:8082/actuator/health>
- Trip Service: <http://localhost:8083/actuator/health>
- Ride Service: <http://localhost:8084/actuator/health>
- Notification Service: <http://localhost:8085/actuator/health>
- API Gateway: <http://localhost:8080/actuator/health>

---

## Troubleshooting

### Service won't start

- Check if MongoDB is running
- Check if the port is already in use
- Check logs for errors

### Can't see service in Eureka

- Wait 30 seconds for registration
- Check Eureka URL in application.yml
- Verify network connectivity

### Swagger UI not loading

- Ensure service is fully started
- Check browser console for errors
- Try clearing browser cache

---

## Stopping Services

### Individual Services

Press `Ctrl+C` in each terminal

### Docker Compose

```bash
docker-compose down
```

---

## Environment Variables

Set these if needed:

```bash
# MongoDB
export MONGODB_URI=mongodb://localhost:27017/carsharing_users

# Google Maps API (for Trip/Ride services)
export GOOGLE_API_KEY=your-api-key-here

# Firebase (for Notification service)
export FIREBASE_CONFIG_PATH=/path/to/firebase.json
export FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
```
