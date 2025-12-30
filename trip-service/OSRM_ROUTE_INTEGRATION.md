# OSRM Route Integration Documentation

## 📋 Overview

This document describes the implementation of OpenStreetMap Routing Machine (OSRM) API integration in the Trip Service. When a driver creates a new ride, the system automatically fetches the complete driving route, calculates distance and duration, and stores this information in MongoDB for later use in ride matching and optimization.

---

## 🎯 What We Implemented

### 1. **OSRM API Integration**
- Integrated with OSRM public routing API
- Fetches real driving routes between two points
- Gets accurate distance and duration estimates
- Retrieves complete route geometry (GeoJSON LineString)

### 2. **Route Data Storage**
- Stores complete route geometry in MongoDB
- Saves distance (meters) and duration (seconds)
- Enables geospatial queries for ride matching
- Maintains route data for trip history and analytics

### 3. **Enhanced Offer Ride Workflow**
- Driver creates ride with source and destination
- System automatically calls OSRM API
- Route calculated and validated
- All data stored in single transaction
- Response includes route details for frontend display

---

## 🏗️ Architecture

```
Driver Request
     ↓
OfferRideController
     ↓
OfferRideServiceImpl
     ↓
OSMRoute Service → OSRM API (router.project-osrm.org)
     ↓                 ↓
     ←─────────────────
     ↓
MongoDB (trips collection)
     ↓
Response with Route Data
```

---

## 📁 File Structure

```
trip-service/
├── config/
│   └── OSMClientConfig.java          # WebClient configuration for OSRM API
├── controller/
│   └── OfferRideController.java      # POST /api/trips/offer endpoint
├── dto/
│   ├── OfferRideRequestDTO.java      # Request structure
│   ├── OfferRideResponseDTO.java     # Response with route data
│   └── OSRM/
│       ├── OsrmResponseDTO.java      # OSRM API response wrapper
│       ├── Route.java                # Route details
│       ├── Geometry.java             # GeoJSON LineString
│       ├── Legs.java                 # Route segments
│       └── Waypoint.java             # Waypoints in route
├── model/
│   └── Trips.java                    # MongoDB entity with route fields
├── repository/
│   └── TripsRepository.java          # MongoDB repository
└── service/
    ├── OfferRideService.java         # Service interface
    ├── routes/
    │   └── OSMRoute.java             # OSRM API client
    └── impl/
        └── OfferRideServiceImpl.java # Business logic with OSRM integration
```

---

## 🔧 Technical Implementation

### 1. WebClient Configuration (`OSMClientConfig.java`)

```java
@Configuration
public class OSMClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://router.project-osrm.org/route/v1/driving/")
                .build();
    }
}
```

**Purpose**: Configures Spring WebFlux WebClient to communicate with OSRM API.

**Why WebClient?**
- Modern Spring approach (replaces RestTemplate)
- Non-blocking I/O support
- Better error handling
- More flexible than RestTemplate

---

### 2. OSRM API Client (`OSMRoute.java`)

```java
@Service
@RequiredArgsConstructor
public class OSMRoute {
    private final WebClient webClient;

    public Mono<OsrmResponseDTO> getRoute(
            double startLat, double startLon, 
            double endLat, double endLon) {
        
        String coordinates = startLon + "," + startLat + ";" + endLon + "," + endLat;
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(coordinates)
                        .queryParam("overview", "full")
                        .queryParam("geometries", "geojson")
                        .build())
                .retrieve()
                .bodyToMono(OsrmResponseDTO.class);
    }
}
```

**API Call Example**:
```
GET https://router.project-osrm.org/route/v1/driving/72.8777,19.0760;73.8567,18.5204?overview=full&geometries=geojson
```

**Parameters**:
- `overview=full` - Returns complete route geometry
- `geometries=geojson` - Returns coordinates in GeoJSON format

**Response Format**:
```json
{
  "code": "Ok",
  "routes": [{
    "geometry": {
      "type": "LineString",
      "coordinates": [[72.8777, 19.0760], [72.88, 19.08], ...]
    },
    "distance": 150000.0,
    "duration": 7200.0,
    "legs": [...]
  }],
  "waypoints": [...]
}
```

---

### 3. OSRM Response DTOs

#### `OsrmResponseDTO.java`
```java
@Data
public class OsrmResponseDTO {
    private String code;              // "Ok" or error code
    private List<Route> routes;       // Array of possible routes
    private List<Waypoint> waypoints; // Start and end points
}
```

#### `Route.java`
```java
@Data
public class Route {
    private Geometry geometry;        // Route path
    private double distance;          // Distance in meters
    private double duration;          // Duration in seconds
    private List<Legs> legs;         // Route segments
    private String weight_name;      // Routing algorithm used
    private double weight;           // Route weight
}
```

#### `Geometry.java`
```java
@Data
public class Geometry {
    private String type;                    // "LineString"
    private List<List<Double>> coordinates; // [[lon, lat], [lon, lat], ...]
}
```

**GeoJSON Format**: Coordinates are in `[longitude, latitude]` order (not lat, lon!)

---

### 4. Enhanced Trips Model

```java
@Document(collection = "trips")
@Data
public class Trips {
    // Existing fields
    private String tripId;
    private String driverId;
    private String vehicleNumber;
    private Points sourceAddress;
    private Points destinationAddress;
    private Instant tripStartDateTimeUTC;
    private String tripTimezone;
    
    // NEW: Route information from OSRM
    private Geometry routeGeometry;   // Full route path as GeoJSON LineString
    private double routeDistance;     // Distance in meters
    private double routeDuration;     // Duration in seconds
    
    // Other fields...
}
```

**Storage Example in MongoDB**:
```json
{
  "_id": "507f1f77bcf86cd799439011",
  "driverId": "driver123",
  "vehicleNumber": "MH12AB1234",
  "routeGeometry": {
    "type": "LineString",
    "coordinates": [
      [72.8777, 19.0760],
      [72.8800, 19.0800],
      [72.9000, 19.1000],
      ...
      [73.8567, 18.5204]
    ]
  },
  "routeDistance": 150000.0,
  "routeDuration": 7200.0,
  "tripStartDateTimeUTC": "2025-12-25T09:00:00Z",
  "tripTimezone": "Asia/Kolkata"
}
```

---

### 5. Service Implementation (`OfferRideServiceImpl.java`)

#### Workflow:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OfferRideServiceImpl implements OfferRideService {
    private final TripsRepository tripsRepository;
    private final OSMRoute osmRoute;

    @Override
    public ResponseDTO<OfferRideResponseDTO> offerRide(
            RequestDTO<OfferRideRequestDTO> request) {
        
        // 1. Extract request data
        OfferRideRequestDTO requestContent = request.getRequestContent();
        
        // 2. Check for time conflicts (existing logic)
        // ... conflict detection code ...
        
        // 3. Call OSRM API to get route
        OsrmResponseDTO osrmResponse = osmRoute.getRoute(
                requestContent.getSourceAddress().getLatitude(),
                requestContent.getSourceAddress().getLongitude(),
                requestContent.getDestinationAddress().getLatitude(),
                requestContent.getDestinationAddress().getLongitude()
        ).block(); // Block to make synchronous
        
        // 4. Validate OSRM response
        if (osrmResponse == null || osrmResponse.getRoutes().isEmpty()) {
            return error("Could not find route");
        }
        
        // 5. Extract route data
        Route route = osrmResponse.getRoutes().get(0);
        
        // 6. Create and save trip with route data
        Trips newTrip = Trips.builder()
                .driverId(request.getUserId())
                .vehicleNumber(requestContent.getVehicleNumber())
                .sourceAddress(requestContent.getSourceAddress())
                .destinationAddress(requestContent.getDestinationAddress())
                .routeGeometry(route.getGeometry())    // Store geometry
                .routeDistance(route.getDistance())    // Store distance
                .routeDuration(route.getDuration())    // Store duration
                .tripStartDateTimeUTC(tripStartInstant)
                .tripTimezone(tripTimezone)
                .build();
        
        Trips savedTrip = tripsRepository.save(newTrip);
        
        // 7. Build response with route info
        responseContent.setRouteDistanceInMeters(savedTrip.getRouteDistance());
        responseContent.setRouteDistanceInKm(savedTrip.getRouteDistance() / 1000.0);
        responseContent.setRouteDurationInSeconds(savedTrip.getRouteDuration());
        responseContent.setRouteDurationInMinutes(savedTrip.getRouteDuration() / 60.0);
        
        return responseDTO;
    }
}
```

---

### 6. Enhanced Response DTO

```java
@Data
public class OfferRideResponseDTO {
    // Trip details
    private String tripId;
    private String vehicleNumber;
    private Points sourceAddress;
    private Points destinationAddress;
    private ZonedDateTime tripStartDateTime;
    private String tripTimezone;
    
    // NEW: Route information
    private Geometry routeGeometry;           // Full route path
    private Double routeDistanceInMeters;     // 150000.0
    private Double routeDistanceInKm;         // 150.0
    private Double routeDurationInSeconds;    // 7200.0
    private Double routeDurationInMinutes;    // 120.0
    
    // Status
    private Boolean tripCreated;
    private String errorMessage;
}
```

---

## 🌐 API Documentation

### Endpoint: Create Offer Ride

**URL**: `POST /api/trips/offer`

**Request Body**:
```json
{
  "userId": "driver123",
  "requestContent": {
    "VehicleNumber": "MH12AB1234",
    "sourceAddress": {
      "latitude": 19.0760,
      "longitude": 72.8777,
      "placeId": "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
      "placeAddress": "Mumbai, Maharashtra, India"
    },
    "destinationAddress": {
      "latitude": 18.5204,
      "longitude": 73.8567,
      "placeId": "ChIJARFGZy6_wjsRQ-Oenb9DjYI",
      "placeAddress": "Pune, Maharashtra, India"
    },
    "tripStartDateTime": "2025-12-25T14:30:00+05:30",
    "offeredSeat": 3
  }
}
```

**Success Response (201 Created)**:
```json
{
  "responseContent": {
    "tripId": "507f1f77bcf86cd799439011",
    "vehicleNumber": "MH12AB1234",
    "sourceAddress": {
      "latitude": 19.0760,
      "longitude": 72.8777,
      "placeAddress": "Mumbai, Maharashtra, India"
    },
    "destinationAddress": {
      "latitude": 18.5204,
      "longitude": 73.8567,
      "placeAddress": "Pune, Maharashtra, India"
    },
    "tripStartDateTime": "2025-12-25T14:30:00+05:30",
    "tripTimezone": "Asia/Kolkata",
    "routeGeometry": {
      "type": "LineString",
      "coordinates": [
        [72.8777, 19.0760],
        [72.8800, 19.0800],
        ...
        [73.8567, 18.5204]
      ]
    },
    "routeDistanceInMeters": 150000.0,
    "routeDistanceInKm": 150.0,
    "routeDurationInSeconds": 7200.0,
    "routeDurationInMinutes": 120.0,
    "tripCreated": true,
    "errorMessage": null
  }
}
```

**Error Response (400 Bad Request)**:
```json
{
  "responseContent": {
    "tripCreated": false,
    "errorMessage": "Could not find a route between source and destination. Please check the addresses."
  }
}
```

**Possible Error Messages**:
- `"Driver already has a trip at the same time. Please choose a different time."`
- `"Could not find a route between source and destination. Please check the addresses."`
- `"Failed to calculate route. Please try again later."`

---

## 🔍 Use Cases

### 1. **Display Route on Map (Frontend)**
```javascript
// Use routeGeometry to draw route on map
const route = response.routeGeometry;
const coordinates = route.coordinates.map(coord => [coord[1], coord[0]]); // Swap to lat,lon

// Using Leaflet.js
const polyline = L.polyline(coordinates, {color: 'blue'}).addTo(map);
map.fitBounds(polyline.getBounds());
```

### 2. **Show Distance and Time**
```javascript
const distance = response.routeDistanceInKm.toFixed(2);
const duration = response.routeDurationInMinutes.toFixed(0);

console.log(`Trip: ${distance} km, approximately ${duration} minutes`);
// Output: "Trip: 150.00 km, approximately 120 minutes"
```

### 3. **Calculate Trip Cost**
```javascript
const pricePerKm = 10.0; // ₹10 per km
const totalCost = response.routeDistanceInKm * pricePerKm;
console.log(`Estimated cost: ₹${totalCost.toFixed(2)}`);
// Output: "Estimated cost: ₹1500.00"
```

### 4. **Ride Matching (Future)**
Use stored route geometry to find riders along the route:
```javascript
// MongoDB Geospatial Query
db.trips.find({
  routeGeometry: {
    $near: {
      $geometry: {
        type: "Point",
        coordinates: [riderLon, riderLat]
      },
      $maxDistance: 5000  // 5km from route
    }
  }
});
```

---

## 📊 Database Schema

### Trips Collection

```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "tripId": "507f1f77bcf86cd799439011",
  "tripStatus": "OFFERED",
  "driverId": "driver123",
  "vehicleNumber": "MH12AB1234",
  
  // Location data
  "sourceAddress": {
    "latitude": 19.0760,
    "longitude": 72.8777,
    "placeId": "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
    "placeAddress": "Mumbai, Maharashtra, India"
  },
  "destinationAddress": {
    "latitude": 18.5204,
    "longitude": 73.8567,
    "placeId": "ChIJARFGZy6_wjsRQ-Oenb9DjYI",
    "placeAddress": "Pune, Maharashtra, India"
  },
  
  // Route data (NEW)
  "routeGeometry": {
    "type": "LineString",
    "coordinates": [[72.8777, 19.0760], [72.88, 19.08], ...]
  },
  "routeDistance": 150000.0,      // meters
  "routeDuration": 7200.0,        // seconds
  
  // Time data
  "tripStartDateTimeUTC": ISODate("2025-12-25T09:00:00Z"),
  "tripTimezone": "Asia/Kolkata",
  "createdDate": ISODate("2025-10-12T10:30:00Z"),
  
  // Capacity
  "offeredSeat": 3,
  "currSeats": 0,
  
  // Pricing
  "pricePerKm": 10.0,
  
  // Riders
  "joinedRidersId": []
}
```

---

## 🗂️ Geospatial Indexes (Required)

Create these indexes in MongoDB for efficient geospatial queries:

```javascript
// In MongoDB Shell or Compass
use carsharing;

// Index on route geometry for route-based queries
db.trips.createIndex({ "routeGeometry": "2dsphere" });

// Index on source location for source-based queries
db.trips.createIndex({ 
  "sourceAddress.latitude": 1, 
  "sourceAddress.longitude": 1 
});

// Index on destination location
db.trips.createIndex({ 
  "destinationAddress.latitude": 1, 
  "destinationAddress.longitude": 1 
});

// Compound index for trip search
db.trips.createIndex({ 
  "tripStatus": 1, 
  "tripStartDateTimeUTC": 1 
});

// Verify indexes
db.trips.getIndexes();
```

**Why Geospatial Indexes?**
- Enable fast proximity searches
- Support route-based matching
- Improve query performance
- Essential for ride-sharing algorithms

---

## ⚡ Performance Considerations

### 1. **OSRM API Calls**
- **Issue**: External API call adds latency (~500ms-2s)
- **Solution**: Consider caching popular routes
- **Future**: Self-host OSRM server for production

### 2. **Route Storage**
- **Size**: Routes can be large (100-1000+ coordinates)
- **Impact**: Increases document size in MongoDB
- **Optimization**: Consider simplifying routes for storage (e.g., Douglas-Peucker algorithm)

### 3. **Geospatial Queries**
- **Requirement**: 2dsphere indexes are REQUIRED
- **Performance**: Indexes make queries 100x faster
- **Monitoring**: Monitor query performance with slow query logs

### 4. **Error Handling**
- **Network Issues**: OSRM API may be unreachable
- **Invalid Routes**: Some locations may not have roads
- **Rate Limits**: Public OSRM has rate limits

---

## 🔒 Error Handling

### OSRM API Errors

```java
try {
    osrmResponse = osmRoute.getRoute(...).block();
    
    if (osrmResponse == null || osrmResponse.getRoutes().isEmpty()) {
        log.error("No route found from OSRM API");
        return error("Could not find route");
    }
} catch (Exception e) {
    log.error("Error calling OSRM API: ", e);
    return error("Failed to calculate route");
}
```

**Possible OSRM Error Codes**:
- `Ok` - Route found successfully
- `NoRoute` - No route between points
- `NoSegment` - Point too far from road network
- `InvalidInput` - Invalid coordinates

---

## 🧪 Testing

### Manual Test with cURL

```bash
curl -X POST http://localhost:8080/trip-service/api/trips/offer \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "driver123",
    "requestContent": {
      "VehicleNumber": "MH12AB1234",
      "sourceAddress": {
        "latitude": 19.0760,
        "longitude": 72.8777,
        "placeAddress": "Mumbai, India"
      },
      "destinationAddress": {
        "latitude": 18.5204,
        "longitude": 73.8567,
        "placeAddress": "Pune, India"
      },
      "tripStartDateTime": "2025-12-25T14:30:00+05:30",
      "offeredSeat": 3
    }
  }'
```

### Test OSRM API Directly

```bash
curl "https://router.project-osrm.org/route/v1/driving/72.8777,19.0760;73.8567,18.5204?overview=full&geometries=geojson"
```

### Expected Response Time
- OSRM API: 200-500ms
- Database Save: 50-100ms
- Total: 300-700ms

---

## 🚀 Future Enhancements

### 1. **Route Caching**
Cache frequently requested routes to reduce API calls:
```java
@Cacheable(value = "routes", key = "#startLat + '-' + #startLon + '-' + #endLat + '-' + #endLon")
public Mono<OsrmResponseDTO> getRoute(...) {
    // ... existing code
}
```

### 2. **Alternative Routes**
OSRM returns multiple route alternatives:
```java
List<Route> allRoutes = osrmResponse.getRoutes();
// Store best route or give user choice
```

### 3. **Route Simplification**
Reduce storage by simplifying route geometry:
```java
List<List<Double>> simplifiedCoords = 
    RouteSimplifier.simplify(route.getGeometry().getCoordinates(), tolerance);
```

### 4. **Self-Hosted OSRM**
For production, host your own OSRM server:
- No rate limits
- Faster response times
- Customizable routing profiles
- Offline capability

### 5. **Advanced Matching**
Use route geometry for intelligent ride matching:
- Find riders along driver's route
- Calculate pickup/dropoff detours
- Optimize multi-pickup routes

---

## 📝 Dependencies Added

### Maven Dependencies

```xml
<!-- WebFlux for WebClient -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Already present -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

---

## 🎓 Key Learnings

### 1. **GeoJSON Coordinate Order**
- ⚠️ **GeoJSON uses [longitude, latitude]** (not lat, lon!)
- API calls use lat,lon: `72.8777,19.0760`
- GeoJSON stores as: `[72.8777, 19.0760]` (lon, lat)

### 2. **Synchronous vs Asynchronous**
- `Mono<T>.block()` makes WebClient synchronous
- For async, return `Mono<ResponseDTO>` from service
- Current implementation is synchronous for simplicity

### 3. **Distance Units**
- OSRM returns distance in **meters**
- Duration in **seconds**
- Convert for display: `distance / 1000` = km, `duration / 60` = minutes

### 4. **Error Handling**
- Always validate OSRM response before using
- Handle network failures gracefully
- Provide meaningful error messages to users

---

## 📞 Support & Troubleshooting

### Common Issues

**1. "Cannot find route" error**
- Check if coordinates are valid (not reversed)
- Ensure locations are accessible by road
- Try coordinates directly in OSRM API

**2. WebClient not found**
- Ensure `spring-boot-starter-webflux` is in pom.xml
- Run `mvn clean install`
- Restart IDE

**3. Route not saving to MongoDB**
- Check MongoDB connection
- Verify Trips model has route fields
- Check for validation errors

**4. OSRM API timeout**
- Public API may be slow/down
- Consider longer timeout: `webClient.timeout(Duration.ofSeconds(10))`
- Or self-host OSRM

---

## 🔗 References

- **OSRM API Documentation**: http://project-osrm.org/docs/v5.24.0/api/
- **GeoJSON Specification**: https://geojson.org/
- **MongoDB Geospatial Queries**: https://docs.mongodb.com/manual/geospatial-queries/
- **Spring WebClient**: https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client

---

## 📅 Implementation Timeline

- **October 12, 2025**: Initial OSRM integration
- **October 12, 2025**: Route storage in MongoDB
- **October 12, 2025**: Enhanced API responses with route data
- **October 12, 2025**: Documentation created

---

## ✅ Checklist for Deployment

- [x] OSRM client configured
- [x] Route DTOs created
- [x] Service layer integrated
- [x] Controller endpoint added
- [x] MongoDB model updated
- [x] Error handling implemented
- [ ] Geospatial indexes created in MongoDB
- [ ] API tested with real data
- [ ] Performance benchmarked
- [ ] Frontend integration tested
- [ ] Production OSRM server considered

---

**Author**: Backend Team  
**Date**: October 12, 2025  
**Version**: 1.0  
**Service**: trip-service  
**Status**: ✅ Implemented and Documented

