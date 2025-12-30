package com.cimhub.tripservice.controller;

import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.TripGeospatialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for geospatial trip searches
 * Allows searching for trips based on location proximity
 */
@RestController
@RequestMapping("/api/trips/search")
@RequiredArgsConstructor
@Slf4j
public class TripSearchController {

    private final TripGeospatialService geoService;

    /**
     * Find trips starting near a given location
     *
     * @param latitude Latitude of search point
     * @param longitude Longitude of search point
     * @param radiusKm Search radius in kilometers (default: 5.0)
     * @return List of trips starting within the radius
     *
     * Example: GET /api/trips/search/near-source?latitude=48.1351&longitude=11.5820&radiusKm=10
     */
    @GetMapping("/near-source")
    public ResponseEntity<List<Trip>> searchNearSource(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {

        log.info("Searching for trips near source: ({}, {}) within {} km", latitude, longitude, radiusKm);
        List<Trip> trips = geoService.findTripsWithSourceNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(trips);
    }

    /**
     * Find trips ending near a given location
     *
     * @param latitude Latitude of search point
     * @param longitude Longitude of search point
     * @param radiusKm Search radius in kilometers (default: 5.0)
     * @return List of trips ending within the radius
     *
     * Example: GET /api/trips/search/near-destination?latitude=48.1351&longitude=11.5820&radiusKm=10
     */
    @GetMapping("/near-destination")
    public ResponseEntity<List<Trip>> searchNearDestination(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {

        log.info("Searching for trips near destination: ({}, {}) within {} km", latitude, longitude, radiusKm);
        List<Trip> trips = geoService.findTripsWithDestinationNear(latitude, longitude, radiusKm);
        return ResponseEntity.ok(trips);
    }

    /**
     * Find trips matching both source and destination within given radius
     * This is useful for finding rides that match a user's journey
     *
     * @param sourceLat Source latitude
     * @param sourceLon Source longitude
     * @param sourceRadiusKm Source search radius in km (default: 5.0)
     * @param destLat Destination latitude
     * @param destLon Destination longitude
     * @param destRadiusKm Destination search radius in km (default: 5.0)
     * @return List of matching trips
     *
     * Example: GET /api/trips/search/matching-route?sourceLat=48.3638&sourceLon=10.6866&sourceRadiusKm=5&destLat=48.1371&destLon=11.5754&destRadiusKm=5
     */
    @GetMapping("/matching-route")
    public ResponseEntity<List<Trip>> searchMatchingRoute(
            @RequestParam double sourceLat,
            @RequestParam double sourceLon,
            @RequestParam(defaultValue = "5.0") double sourceRadiusKm,
            @RequestParam double destLat,
            @RequestParam double destLon,
            @RequestParam(defaultValue = "5.0") double destRadiusKm,
            @RequestParam String rideStartTime,
            @RequestParam Integer requestedSeats,
            @RequestParam String effectiveUserId
            ) {

        log.info("Searching for trips from ({}, {}) [{}km] to ({}, {}) [{}km]",
                sourceLat, sourceLon, sourceRadiusKm, destLat, destLon, destRadiusKm);

        List<Trip> trips = geoService.findTripsMatchingRoute(
                sourceLat, sourceLon, sourceRadiusKm,
                destLat, destLon, destRadiusKm, rideStartTime, requestedSeats, effectiveUserId
        );
        return ResponseEntity.ok(trips);
    }

    /**
     * Find trips starting within a bounding box area
     * Useful for finding all trips in a specific region
     *
     * @param minLat Minimum latitude (southwest corner)
     * @param minLon Minimum longitude (southwest corner)
     * @param maxLat Maximum latitude (northeast corner)
     * @param maxLon Maximum longitude (northeast corner)
     * @return List of trips within the area
     *
     * Example: GET /api/trips/search/in-area?minLat=47.0&minLon=10.0&maxLat=50.0&maxLon=13.0
     */
    @GetMapping("/in-area")
    public ResponseEntity<List<Trip>> searchInArea(
            @RequestParam double minLat,
            @RequestParam double minLon,
            @RequestParam double maxLat,
            @RequestParam double maxLon) {

        log.info("Searching for trips in area: ({}, {}) to ({}, {})", minLat, minLon, maxLat, maxLon);
        List<Trip> trips = geoService.findTripsInArea(minLat, minLon, maxLat, maxLon);
        return ResponseEntity.ok(trips);
    }
}

