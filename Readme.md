
# Car Sharing Application

A carpooling platform designed for Tanzania to facilitate ride sharing between students in the university.

## Project Overview

This application provides a robust backend for a carpooling service, allowing users to offer and find rides, manage vehicles, and track ride history. It leverages a microservices architecture for scalability and efficiency.

## 🚀 Quick Start

### Prerequisites

- **Java 21+** and **Maven 3.6+**
- **MongoDB Atlas** account (or local MongoDB instance)
- **Google Maps API** key
- **Firebase** account with Realtime Database (optional, for notifications)

### Environment Setup

1. **Clone the repository** and navigate to the project directory
2. **Copy environment template**: `cp .env.example .env`
3. **Configure `.env` file** with your credentials:
   - `MONGODB_URI`: Your MongoDB connection string
   - `JWT_SECRET_KEY`: Secret key for JWT authentication
   - `GOOGLE_API_KEY`: Your Google Maps API key
   - `FIREBASE_CONFIG_PATH` and `FIREBASE_DATABASE_URL`: Firebase configuration (optional)

### Running the Application

#### Option 1: Docker Compose (Recommended)

```bash
docker-compose up -d
```

#### Option 2: Individual Services

```bash
# Start Eureka Server first
cd eureka-server && mvn spring-boot:run

# Then start other services in separate terminals
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
# ... and so on
```

#### Access Points

- **API Gateway (Aggregated Swagger)**: <http://localhost:8080/swagger-ui.html>
- **Eureka Dashboard**: <http://localhost:8761>

For detailed setup instructions, see [Getting Started & Running](.docs/running.md).

## 📖 Documentation

For detailed information, please refer to the following documents:

- **[Architecture & Technology Stack](.docs/architecture.md)** - Overview of microservices, tech stack, and key technical capabilities (Geospatial, Timezone, OSRM).
- **[API Guide & Integration](.docs/api-guide.md)** - Detailed instructions on how to use the APIs, authentication, and key endpoint examples.
- **[Getting Started & Running](.docs/running.md)** - Setup instructions, environment variables, and how to run the services.

## 📊 Presentations

Project presentations demonstrating the car sharing platform:

- [Car Sharing Presentation](.docs/presentations/CarSharing.pptx)
- [Backend Implementation Details](.docs/presentations/CarSharing_Backend.pptx)

## Contributors

- Vipul Singh (<singhv@students.uni-marburg.de>)
- Hikmat Samadov (<Samadov@students.uni-marburg.de>)

## License

This project is licensed under the Apache 2.0 License.
