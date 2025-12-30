package com.singhv.tripservice.service;

import com.singhv.tripservice.model.Trips;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for geospatial queries on trips
 * Allows searching for trips near a location
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripGeospatialService {

    private final MongoTemplate mongoTemplate;

    /**
     * Find trips with source location near the given coordinates
     *
     * @param latitude      Latitude of search point
     * @param longitude     Longitude of search point
     * @param radiusInKm    Search radius in kilometers
     * @return List of trips with source near the search point
     */
    public List<Trips> findTripsWithSourceNear(double latitude, double longitude, double radiusInKm) {
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusInKm, Metrics.KILOMETERS);

        Query query = new Query();
        query.addCriteria(Criteria.where("sourceLocation")
                .nearSphere(location)
                .maxDistance(distance.getNormalizedValue()));
        query.addCriteria(Criteria.where("tripStatus").is("OFFERED"));

        List<Trips> trips = mongoTemplate.find(query, Trips.class);
        log.info("Found {} trips with source near ({}, {}) within {} km",
                trips.size(), latitude, longitude, radiusInKm);

        return trips;
    }

    /**
     * Find trips with destination location near the given coordinates
     *
     * @param latitude      Latitude of search point
     * @param longitude     Longitude of search point
     * @param radiusInKm    Search radius in kilometers
     * @return List of trips with destination near the search point
     */
    public List<Trips> findTripsWithDestinationNear(double latitude, double longitude, double radiusInKm) {
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusInKm, Metrics.KILOMETERS);

        Query query = new Query();
        query.addCriteria(Criteria.where("destinationLocation")
                .nearSphere(location)
                .maxDistance(distance.getNormalizedValue()));
        query.addCriteria(Criteria.where("tripStatus").is("OFFERED"));

        List<Trips> trips = mongoTemplate.find(query, Trips.class);
        log.info("Found {} trips with destination near ({}, {}) within {} km",
                trips.size(), latitude, longitude, radiusInKm);

        return trips;
    }

    /**
     * Find trips matching both source and destination within given radius
     * Note: MongoDB doesn't allow multiple $geoNear operations in one query,
     * so we search by source first, then filter by destination in memory
     *
     * @param sourceLat         Source latitude
     * @param sourceLon         Source longitude
     * @param sourceRadiusKm    Source search radius in km
     * @param destLat           Destination latitude
     * @param destLon           Destination longitude
     * @param destRadiusKm      Destination search radius in km
     * @return List of matching trips
     */
    public List<Trips> findTripsMatchingRoute(
            double sourceLat, double sourceLon, double sourceRadiusKm,
            double destLat, double destLon, double destRadiusKm, String rideStartTime, Integer requestedSeats, String effectiveUserId) {
        
        // Step 0: Parse the rideStartTime and calculate the date range for filtering
        Instant startOfDay = null;
        Instant endOfDay = null;
        
        if (rideStartTime != null && !rideStartTime.isEmpty()) {
            try {
                ZonedDateTime requestedDateTime;
                
                // Try to parse with timezone first, if fails, use system timezone
                if (rideStartTime.contains("+") || rideStartTime.contains("Z")) {
                    // Has timezone info
                    requestedDateTime = ZonedDateTime.parse(rideStartTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } else {
                    // No timezone, assume system timezone
                    LocalDateTime localDateTime = LocalDateTime.parse(rideStartTime);
                    requestedDateTime = localDateTime.atZone(ZoneId.systemDefault());
                    log.info("No timezone in input, using system default: {}", ZoneId.systemDefault());
                }
                
                // Get the start and end of that day
                LocalDate requestedDate = requestedDateTime.toLocalDate();
                ZoneId timezone = requestedDateTime.getZone();
                
                startOfDay = requestedDate.atStartOfDay(timezone).toInstant();
                endOfDay = requestedDate.plusDays(1).atStartOfDay(timezone).toInstant();
                
                log.info("Filtering trips for date: {} in timezone: {}. UTC range: {} to {}", 
                        requestedDate, timezone, startOfDay, endOfDay);
            } catch (Exception e) {
                log.error("Failed to parse rideStartTime: {}. Skipping date filter. Error: {}", 
                        rideStartTime, e.getMessage());
            }
        } else {
            log.warn("No rideStartTime provided. Returning all trips (with valid timestamps only).");
        }
        
        // Make final variables for lambda
        final Instant finalStartOfDay = startOfDay;
        final Instant finalEndOfDay = endOfDay;
        
        // Step 1: Find trips with source near the pickup point
        Point sourceLocation = new Point(sourceLon, sourceLat);
        Distance sourceDistance = new Distance(sourceRadiusKm, Metrics.KILOMETERS);

        Query query = new Query();
        query.addCriteria(Criteria.where("sourceLocation")
                .nearSphere(sourceLocation)
                .maxDistance(sourceDistance.getNormalizedValue()));
        query.addCriteria(Criteria.where("tripStatus").is("OFFERED"));
        
        // Add date range filter if we have valid start/end times
        // This will also automatically filter out trips with null tripStartDateTimeUTC
        if (startOfDay != null && endOfDay != null) {
            query.addCriteria(Criteria.where("tripStartDateTimeUTC")
                    .gte(startOfDay)
                    .lt(endOfDay)
                    .ne(null));  // Combined: date range AND not null
        } else {
            // No date filter, but still exclude old trips without timestamp
            query.addCriteria(Criteria.where("tripStartDateTimeUTC").ne(null));
        }
        
        // Step 0.1: Filter by available seats (offered seats should be >= requested seats)
        // This means: (offeredSeat - currSeats) >= requestedSeats
        // Which translates to: availableSeats >= requestedSeats
        if (requestedSeats != null && requestedSeats > 0) {
            // We need to filter in-memory since MongoDB doesn't support computed field queries directly
            // But we can at least ensure offeredSeat >= requestedSeats as a baseline
            query.addCriteria(Criteria.where("offeredSeat").gte(requestedSeats));
        }
        
        // Step 0.2: Exclude trips created by the current user (don't show user their own trips)
        if (effectiveUserId != null && !effectiveUserId.isEmpty()) {
            query.addCriteria(Criteria.where("driverId").ne(effectiveUserId));
        }

        List<Trips> tripsNearSource = mongoTemplate.find(query, Trips.class);

        log.info("Found {} trips near source ({}, {}) within {} km after initial filtering",
                tripsNearSource.size(), sourceLat, sourceLon, sourceRadiusKm);

        // Step 2: Filter results by destination proximity and available seats
        double destRadiusInMeters = destRadiusKm * 1000;

        List<Trips> matchingTrips = tripsNearSource.stream()
                .filter(trip -> {
                    // Safety check: Skip trips without valid tripStartDateTimeUTC
                    if (trip.getTripStartDateTimeUTC() == null) {
                        log.warn("Skipping trip {} - missing tripStartDateTimeUTC", trip.getTripId());
                        return false;
                    }
                    
                    // Double-check date range if specified (defense in depth)
                    if (finalStartOfDay != null && finalEndOfDay != null) {
                        Instant tripTime = trip.getTripStartDateTimeUTC();
                        if (tripTime.isBefore(finalStartOfDay) || !tripTime.isBefore(finalEndOfDay)) {
                            log.debug("Skipping trip {} - outside date range", trip.getTripId());
                            return false;
                        }
                    }
                    
                    // Check destination proximity
                    if (trip.getDestinationLocation() == null) {
                        return false;
                    }
                    double distance = calculateDistance(
                            destLat, destLon,
                            trip.getDestinationLocation().getY(),
                            trip.getDestinationLocation().getX()
                    );
                    
                    if (distance > destRadiusInMeters) {
                        return false;
                    }
                    
                    // Step 0.1 (continued): Check actual available seats
                    // availableSeats = offeredSeat - currSeats
                    if (requestedSeats != null && requestedSeats > 0) {
                        int availableSeats = trip.getOfferedSeat() - trip.getCurrSeats();
                        if (availableSeats < requestedSeats) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .toList();

        log.info("Found {} trips matching route from ({}, {}) to ({}, {}) within {} km and {} km with {} requested seats for user {}",
                matchingTrips.size(), sourceLat, sourceLon, destLat, destLon, sourceRadiusKm, destRadiusKm, requestedSeats, effectiveUserId);

        return matchingTrips;
    }

    /**
     * Find trips within a specific area (bounding box)
     *
     * @param minLat Minimum latitude
     * @param minLon Minimum longitude
     * @param maxLat Maximum latitude
     * @param maxLon Maximum longitude
     * @return List of trips with source within the bounding box
     */
    public List<Trips> findTripsInArea(double minLat, double minLon, double maxLat, double maxLon) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sourceLocation")
                .within(new org.springframework.data.mongodb.core.geo.GeoJsonPolygon(
                        new Point(minLon, minLat),
                        new Point(maxLon, minLat),
                        new Point(maxLon, maxLat),
                        new Point(minLon, maxLat),
                        new Point(minLon, minLat)
                )));
        query.addCriteria(Criteria.where("tripStatus").is("OFFERED"));

        List<Trips> trips = mongoTemplate.find(query, Trips.class);
        log.info("Found {} trips in area ({}, {}) to ({}, {})",
                trips.size(), minLat, minLon, maxLat, maxLon);

        return trips;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @return distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
