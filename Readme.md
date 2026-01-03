
# Car Sharing Application

A carpooling platform designed for Tanzania to facilitate ride sharing between students in the university.

## Project Overview

This application provides a robust backend for a carpooling service, allowing users to offer and find rides, manage vehicles, and track ride history. It leverages a **Kubernetes-native microservices architecture** for scalability and efficiency.

## 🚀 Quick Start

### Prerequisites

- **Docker Desktop** (or Docker Engine)
- **Kind**: [Installation Guide](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)
- **kubectl**: [Installation Guide](https://kubernetes.io/docs/tasks/tools/)
- **Java 21+** and **Maven 3.6+**
- **MongoDB Atlas** account (or local MongoDB instance)
- **Google Maps API** key
- **Firebase** account with Realtime Database (optional, for notifications)

### Local Development with Kind

1. **Clone the repository** and navigate to the project directory

2. **Copy environment template**:

   ```bash
   cp .env.example .env
   ```

3. **Configure `.env` file** with your credentials:
   - `MONGODB_URI`: Your MongoDB connection string
   - `JWT_SECRET_KEY`: Secret key for JWT authentication
   - `GOOGLE_API_KEY`: Your Google Maps API key
   - `FIREBASE_CONFIG_PATH` and `FIREBASE_DATABASE_URL`: Firebase configuration (optional)

4. **Set up Kind cluster**:

   ```bash
   ./scripts/local/setup-kind.sh
   ```

5. **Create secrets**:

   ```bash
   ./scripts/local/create-secrets.sh
   kubectl apply -f k8s/base/secrets.yaml
   ```

6. **Build and deploy**:

   ```bash
   ./scripts/local/build-for-kind.sh
   kubectl apply -k k8s/overlays/local/
   ```

7. **Verify deployment**:

   ```bash
   kubectl get pods -n carsharing
   ```

#### Access Points

- **Swagger UI**: <http://localhost:8080/swagger-ui.html>
- **API Endpoints**: <http://localhost:8080/api/>*

For detailed local development instructions, see [Running Guide](.docs/running.md).

## 📖 Documentation

For detailed information, please refer to the following documents:

1. **[Getting Started & Running](.docs/running.md)**  
   Setup instructions, environment variables, and how to run the services.

2. **[Architecture & Technology Stack](.docs/architecture.md)**  
   Overview of microservices, tech stack, and key technical capabilities (Geospatial, Timezone, OSRM).

3. **[API Guide & Integration](.docs/api-guide.md)**  
   Detailed instructions on how to use the APIs, authentication, and key endpoint examples.

4. **[User Flows](.docs/product/user-flows.md)**  
   Detailed user journeys for passengers and drivers.

5. **[Product Roadmap](.docs/product/roadmap.md)**  
   Planned features and enhancements for future development.

6. **[Changelog](.docs/changelog.md)**  
   Version history and updates.

## 📊 Presentations

Project presentations demonstrating the car sharing platform:

- [Car Sharing Presentation](.docs/presentations/SS_2025/CarSharing.pptx)
- [Backend Implementation Details](.docs/presentations/SS_2025/CarSharing_Backend.pptx)

## Contributors

- Hikmat Samadov (<Samadov@students.uni-marburg.de>)
- Vipul Singh (<singhv@students.uni-marburg.de>)

## License

This project is licensed under the Apache 2.0 License.
