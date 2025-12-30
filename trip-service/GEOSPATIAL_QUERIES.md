# Geospatial Query Support for Trip Service

## Overview
The trip service now supports MongoDB geospatial queries to efficiently find trips based on location proximity. This allows you to search for:
- Trips starting near a specific location
- Trips ending near a specific location
- Trips matching both source and destination within a radius
- Trips within a bounding box area

## What Changed

### 1. Database Schema
Each trip document now includes GeoJSON Point fields alongside the existing address fields:

```json
{
  "_id": "68ec0a7968f0c407b33556cc",
  "tripStatus": "OFFERED",
  "driverId": "vip@gmail.com",
  "vehicleNumber": "ABC123",
  
  // Original address fields (for display) - UNCHANGED
  "sourceAddress": {
    "latitude": 48.3638222,
    "longitude": 10.6866494,
    "placeAddress": "Augsburg (district), Bavaria, Germany"
  },
  "destinationAddress": {
    "latitude": 48.1371079,
    "longitude": 11.5753822,
    "placeAddress": "Munich, Bavaria, Germany"
  },
  
  // NEW: GeoJSON Point fields (for geospatial queries)
  "sourceLocation": {
    "type": "Point",
    "coordinates": [10.6866494, 48.3638222]  // [longitude, latitude]
  },
  "destinationLocation": {
    "type": "Point",
    "coordinates": [11.5753822, 48.1371079]
  },
  
  "routeGeometry": {
    "type": "LineString",
    "coordinates": [[10.6866494, 48.3638222], [11.5753822, 48.1371079]]
  },
  "routeDistance": 83654.7,
  "routeDuration": 3729.7
}
```

### 2. Automatic Conversion
The backend automatically converts `sourceAddress` and `destinationAddress` to GeoJSON `Point` fields when saving trips. **No frontend changes required!**

### 3. Geospatial Indexes
MongoDB 2dsphere indexes are automatically created on:
- `sourceLocation`
- `destinationLocation`
- `routeGeometry`

These indexes enable fast geospatial queries.

## How to Use

### Service: `TripGeospatialService`

#### 1. Find trips with source near a location
```java
@Autowired
private TripGeospatialService geoService;

// Find trips starting within 5km of Munich city center
List<Trips> trips = geoService.findTripsWithSourceNear(
    48.1351, // latitude
    11.5820, // longitude
    5.0      // radius in km
);
```

#### 2. Find trips with destination near a location
```java
// Find trips ending within 10km of Berlin
List<Trips> trips = geoService.findTripsWithDestinationNear(
    52.5200, // latitude
    13.4050, // longitude
    10.0     // radius in km
);
```

#### 3. Find trips matching both source and destination
```java
// Find trips from near Augsburg to near Munich
List<Trips> trips = geoService.findTripsMatchingRoute(
    48.3638, 10.6866, 5.0,  // source: lat, lon, radius
    48.1371, 11.5754, 5.0   // dest: lat, lon, radius
);
```

#### 4. Find trips in a bounding box
```java
// Find all trips starting in Bavaria region
List<Trips> trips = geoService.findTripsInArea(
    47.0, 10.0,  // min lat, min lon (southwest corner)
    50.0, 13.0   // max lat, max lon (northeast corner)
);
```

## Example API Endpoints

You can create REST endpoints to expose these queries:

```java
@RestController
@RequestMapping("/api/trips/search")
@RequiredArgsConstructor
public class TripSearchController {

    private final TripGeospatialService geoService;

    @GetMapping("/near-source")
    public ResponseEntity<List<Trips>> searchNearSource(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        
        List<Trips> trips = geoService.findTripsWithSourceNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/matching-route")
    public ResponseEntity<List<Trips>> searchMatchingRoute(
            @RequestParam double sourceLat,
            @RequestParam double sourceLon,
            @RequestParam(defaultValue = "5.0") double sourceRadiusKm,
            @RequestParam double destLat,
            @RequestParam double destLon,
            @RequestParam(defaultValue = "5.0") double destRadiusKm) {
        
        List<Trips> trips = geoService.findTripsMatchingRoute(
            sourceLat, sourceLon, sourceRadiusKm,
            destLat, destLon, destRadiusKm
        );
        return ResponseEntity.ok(trips);
    }
}
```

## Important Notes

### GeoJSON Coordinate Order
⚠️ **GeoJSON uses [longitude, latitude]** order, NOT [latitude, longitude]!
- Most APIs use [lat, lon]
- GeoJSON uses [lon, lat]
- The service handles this conversion automatically

### Distance Units
- All distances are in **kilometers** by default
- MongoDB stores distances in **meters** internally
- The service converts automatically

### Query Performance
- Geospatial queries are optimized with 2dsphere indexes
- Typical query time: < 100ms for millions of documents
- Always limit results if returning to frontend

### Frontend Integration
**No changes needed!** 
- Frontend continues to send latitude/longitude in `sourceAddress` and `destinationAddress`
- Backend automatically creates GeoJSON fields
- Frontend receives the same response format

## Testing

You can test geospatial queries in MongoDB Compass or shell:

```javascript
// Find trips starting within 5km of Munich
db.trips.find({
  sourceLocation: {
    $nearSphere: {
      $geometry: {
        type: "Point",
        coordinates: [11.5820, 48.1351]  // [lon, lat]
      },
      $maxDistance: 5000  // meters
    }
  }
})
```

## Migration

For existing trips without GeoJSON fields:
1. The indexes are created automatically on startup
2. New trips will have GeoJSON fields automatically
3. Old trips can be updated with a migration script (optional)

```javascript
// MongoDB migration script to add GeoJSON to existing trips
db.trips.find({ sourceLocation: { $exists: false } }).forEach(trip => {
  db.trips.updateOne(
    { _id: trip._id },
    {
      $set: {
        sourceLocation: {
          type: "Point",
          coordinates: [
            trip.sourceAddress.longitude,
            trip.sourceAddress.latitude
          ]
        },
        destinationLocation: {
          type: "Point",
          coordinates: [
            trip.destinationAddress.longitude,
            trip.destinationAddress.latitude
          ]
        }
      }
    }
  );
});
```

## Benefits

✅ **Fast proximity searches** - Find trips near any location in milliseconds  
✅ **No frontend changes** - Backend handles all conversions  
✅ **Flexible radius** - Search within any distance  
✅ **Route matching** - Match both source and destination  
✅ **Scalable** - Works efficiently with millions of trips  
✅ **Standard GeoJSON** - Compatible with mapping libraries  

## Next Steps

Consider adding:
- Search by route path (find trips passing through an area)
- Distance sorting (nearest first)
- Combining with other filters (date, seats available, price)
- Caching popular search results

