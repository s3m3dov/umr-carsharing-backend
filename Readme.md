
# Car Sharing Application
A carpooling platform designed for Tanzania to facilitate ride sharing between students in the university.
## Contributors
- Vipul Singh (singhv@students.uni-marburg.de)
## Project Overview
This application provides a robust backend for a carpooling service, allowing users to:
- Offer rides (create trips)
- Find available rides
- Join existing trips
- Manage their vehicles
- View ride history and upcoming rides
- Cancel rides

The system leverages Google Maps API for location services and distance calculations, Firebase for real-time data storage, and MongoDB for persistent data storage.

## Technology Stack
- **Java**: Java 22
- **Framework**: Spring Boot with Spring MVC
- **Database**:
    - MongoDB Atlas (for persistent storage)
    - Firebase Realtime Database (for real-time data)

- **APIs**:
    - Google Maps API (for directions and distance calculations)

- **Other Libraries**:
    - Lombok (for reducing boilerplate code)
    - Spring Data Mongo (for MongoDB integration)
    - Jakarta EE

## API Endpoints
The application exposes several REST endpoints, organized into the following controllers:

### User Management
- `/api/users/{userId}` - Get users information
- `/api/users/signup` - Register a new users
- `/api/users/login` - Authenticate a users

### Vehicle Management
- `/api/vehicles/{userId}` - Get users's vehicles
- `/api/vehicles/register` - Register a new vehicle

### Ride Operations
- `/api/ride/create-trip` - Create a new trip (offer a ride)
- `/api/rides/find-ride` - Find available rides
- `/api/rides/join-trip` - Join an existing trip

### My Rides
- `/api/myrides/upcoming` - Get users's upcoming rides
- `/api/myrides/history` - Get users's ride history
- `/api/myrides/cancel` - Cancel a ride

## Setup and Configuration

### Prerequisites
- Java 22 JDK
- MongoDB Atlas account
- Firebase account with Realtime Database
- Google Maps API key

### Configuration Files
The project uses the following configuration files:
1. **application.properties**: Main configuration file (excluded from git for security)
2. **application-sample.properties**: Template for the main configuration
3. **firebase-sdk.json**: Firebase credentials (excluded from git for security)
4. **firebase-sdk-sample.json**: Template for Firebase credentials

### Getting Started
1. Clone the repository
2. Copy `application-sample.properties` to `application.properties` and update with your credentials
3. Copy `firebase-sdk-sample.json` to `firebase-sdk.json` and update with your Firebase credentials
4. Build the project using Maven:
``` 
./mvnw clean install
```
5. Run the application:
``` 
./mvnw spring-boot:run
```

## Environment Variables
The following environment variables or properties are required:
- `GOOGLE_API_KEY`: Your Google Maps API key
- `firebase.config.path`: Path to your Firebase SDK JSON file
- `firebase.database.url`: Your Firebase Realtime Database URL
- `spring.data.mongodb.uri`: MongoDB connection string
- `spring.data.mongodb.database`: MongoDB database name

## Security Considerations
- Sensitive configuration files are excluded from version control (see .gitignore)
- Sample configuration files are provided as templates
- API keys and database credentials should be kept secure

## Development Notes
- CORS is configured to allow requests from a specific origin (`http://192.168.0.230:3000`)
- The server runs on port 8911 by default
- The application handles complex distance calculations for matching rides

## Demo and Frontend
There is a presentation of the backend implementation along with the frontend. You can view the working project there too.

```aiignore
Car Sharing.ppt
```

## License
This project is licensed under the Apache 2.0 License.
