package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.OSRM.Geometry;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
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

    public void enrichTripWithGeospatialData(Trip trip, com.cimhub.tripservice.dto.OSRM.Route route) {
        if (trip == null || route == null)
            return;

        Geometry geometry = route.geometry();
        if (geometry != null) {
            trip.setRouteGeometry(geometry);
        }
        trip.setRouteDistance(route.distance());
        trip.setRouteDuration(route.duration());

        // Update GeoSpatial indexes
        if (trip.getSourceAddress() != null) {
            trip.setSourceLocation(new Point(
                    trip.getSourceAddress().getLongitude(),
                    trip.getSourceAddress().getLatitude()));
        }

        if (trip.getDestinationAddress() != null) {
            trip.setDestinationLocation(new Point(
                    trip.getDestinationAddress().getLongitude(),
                    trip.getDestinationAddress().getLatitude()));
        }
    }

    /**
     * Find trips with source location near the given coordinates
     *
     * @param latitude   Latitude of search point
     * @param longitude  Longitude of search point
     * @param radiusInKm Search radius in kilometers
     * @return List of trips with source near the search point
     */
    public List<Trip> findTripsWithSourceNear(double latitude, double longitude, double radiusInKm) {
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusInKm, Metrics.KILOMETERS);

        Query query = new Query();
        query.addCriteria(Criteria.where("sourceLocation")
                .nearSphere(location)
                .maxDistance(distance.getNormalizedValue()));
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("tripStatus").is(TripStatus.CREATED),
                Criteria.where("tripStatus").is(TripStatus.AVAILABLE)));

        List<Trip> trips = mongoTemplate.find(query, Trip.class);
        log.info("Found {} trips with source near ({}, {}) within {} km",
                trips.size(), latitude, longitude, radiusInKm);

        return trips;
    }

    /**
     * Find trips with destination location near the given coordinates
     *
     * @param latitude   Latitude of search point
     * @param longitude  Longitude of search point
     * @param radiusInKm Search radius in kilometers
     * @return List of trips with destination near the search point
     */
    public List<Trip> findTripsWithDestinationNear(double latitude, double longitude, double radiusInKm) {
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusInKm, Metrics.KILOMETERS);

        Query query = new Query();
        query.addCriteria(Criteria.where("destinationLocation")
                .nearSphere(location)
                .maxDistance(distance.getNormalizedValue()));
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("tripStatus").is(TripStatus.CREATED),
                Criteria.where("tripStatus").is(TripStatus.AVAILABLE)));

        List<Trip> trips = mongoTemplate.find(query, Trip.class);
        log.info("Found {} trips with destination near ({}, {}) within {} km",
                trips.size(), latitude, longitude, radiusInKm);

        return trips;
    }

    /**
     * Find trips matching both source and destination within given radius
     * Note: MongoDB doesn't allow multiple $geoNear operations in one query,
     * so we search by source first, then filter by destination in memory
     *
     * @param sourceLatitude        Source latitude
     * @param sourceLongitude       Source longitude
     * @param sourceRadiusKm        Source search radius in km
     * @param destinationLatitude   Destination latitude
     * @param destinationLongitude  Destination longitude
     * @param destRadiusKm          Destination search radius in km
     * @param earliestDepartureTime (Optional) Earliest departure time for range
     *                              filtering (ISO 8601 format)
     * @param latestDepartureTime   (Optional) Latest departure time for range
     *                              filtering (ISO 8601 format)
     * @param requestedSeats        (Optional) Number of seats requested
     * @param effectiveUserId       (Optional) ID of the user making the request (to
     *                              exclude their own trips)
     * @return List of matching trips
     */
    public List<Trip> findTripsMatchingRoute(
            double sourceLatitude, double sourceLongitude, double sourceRadiusKm,
            double destinationLatitude, double destinationLongitude, double destRadiusKm,
            String earliestDepartureTime,
            String latestDepartureTime,
            Integer requestedSeats,
            String effectiveUserId,
            Double minPrice,
            Double maxPrice,
            String carType,
            Integer minAvailableSeats) {

        // Step 0: Parse time parameters and calculate the search range
        Instant searchWindowStart = null;
        Instant searchWindowEnd = null;

        // Priority 1: Use range parameters if provided
        if (earliestDepartureTime != null || latestDepartureTime != null) {
            try {
                if (earliestDepartureTime != null) {
                    searchWindowStart = parseDateTime(earliestDepartureTime);
                    log.info("Earliest departure time: {}", searchWindowStart);
                }
                if (latestDepartureTime != null) {
                    searchWindowEnd = parseDateTime(latestDepartureTime);
                    log.info("Latest departure time: {}", searchWindowEnd);
                }

                // Validation: ensure latest is after earliest
                if (searchWindowStart != null && searchWindowEnd != null
                        && !searchWindowStart.isBefore(searchWindowEnd)) {
                    throw new IllegalArgumentException(
                            "latestDepartureTime must be after earliestDepartureTime");
                }

                log.info("Using time range filter: {} to {}", searchWindowStart, searchWindowEnd);
            } catch (IllegalArgumentException e) {
                throw e; // Re-throw validation errors
            } catch (Exception e) {
                log.error("Failed to parse time range parameters. Error: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid time range parameters: " + e.getMessage());
            }
        }
        // Priority 2: No time filter - search all future trips
        else {
            searchWindowStart = Instant.now();
            log.info("No time parameters provided. Searching all future trips from: {}", searchWindowStart);
        }

        // Make final variables for lambda
        final Instant finalSearchWindowStart = searchWindowStart;
        final Instant finalSearchWindowEnd = searchWindowEnd;

        // Step 1: Find trips with source near the pickup point
        Point sourceLocation = new Point(sourceLongitude, sourceLatitude);
        Distance sourceDistance = new Distance(sourceRadiusKm, Metrics.KILOMETERS);

        Query query = new Query();
        query.addCriteria(Criteria.where("sourceLocation")
                .nearSphere(sourceLocation)
                .maxDistance(sourceDistance.getNormalizedValue()));
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("tripStatus").is(TripStatus.CREATED),
                Criteria.where("tripStatus").is(TripStatus.AVAILABLE)));

        // Add time range filter
        // This will also automatically filter out trips with null tripStartDateTimeUTC
        if (searchWindowStart != null && searchWindowEnd != null) {
            query.addCriteria(Criteria.where("tripStartDateTimeUTC")
                    .gte(searchWindowStart)
                    .lt(searchWindowEnd)
                    .ne(null));
        } else if (searchWindowStart != null) {
            // Only earliest time specified - open-ended end
            query.addCriteria(Criteria.where("tripStartDateTimeUTC")
                    .gte(searchWindowStart)
                    .ne(null));
        } else if (searchWindowEnd != null) {
            // Only latest time specified - open-ended start
            query.addCriteria(Criteria.where("tripStartDateTimeUTC")
                    .lt(searchWindowEnd)
                    .ne(null));
        } else {
            // No time filter, but still exclude trips without timestamp
            query.addCriteria(Criteria.where("tripStartDateTimeUTC").ne(null));
        }

        // Step 0.1: Filter by available seats (offered seats should be >= requested
        // seats)
        // This means: (totalSeats - bookedSeats) >= requestedSeats
        // Which translates to: availableSeats >= requestedSeats
        if (requestedSeats != null && requestedSeats > 0) {
            // We need to filter in-memory since MongoDB doesn't support computed field
            // queries directly
            // But we can at least ensure totalSeats >= requestedSeats as a baseline
            query.addCriteria(Criteria.where("totalSeats").gte(requestedSeats));
        }

        // Step 0.2: Exclude trips created by the current user (don't show user their
        // own trips)
        if (effectiveUserId != null && !effectiveUserId.isEmpty()) {
            query.addCriteria(Criteria.where("driverId").ne(effectiveUserId));
        }

        // Add price range filter
        if (minPrice != null && maxPrice != null) {
            query.addCriteria(Criteria.where("pricePerSeat").gte(minPrice).lte(maxPrice));
        } else if (minPrice != null) {
            query.addCriteria(Criteria.where("pricePerSeat").gte(minPrice));
        } else if (maxPrice != null) {
            query.addCriteria(Criteria.where("pricePerSeat").lte(maxPrice));
        }

        // Add car type filter
        if (carType != null && !carType.isEmpty()) {
            query.addCriteria(Criteria.where("carType").is(carType));
        }

        List<Trip> tripsNearSource = mongoTemplate.find(query, Trip.class);

        log.info("Found {} trips near source ({}, {}) within {} km after initial filtering",
                tripsNearSource.size(), sourceLatitude, sourceLongitude, sourceRadiusKm);

        // Step 2: Filter results by destination proximity and available seats
        double destRadiusInMeters = destRadiusKm * 1000;

        List<Trip> matchingTrips = tripsNearSource.stream()
                .filter(trip -> {
                    // Safety check: Skip trips without valid tripStartDateTimeUTC
                    if (trip.getTripStartDateTimeUTC() == null) {
                        log.warn("Skipping trip {} - missing tripStartDateTimeUTC", trip.getTripId());
                        return false;
                    }

                    // Double-check time range if specified (defense in depth)
                    if (finalSearchWindowStart != null && finalSearchWindowEnd != null) {
                        Instant tripTime = trip.getTripStartDateTimeUTC();
                        if (tripTime.isBefore(finalSearchWindowStart) || !tripTime.isBefore(finalSearchWindowEnd)) {
                            log.debug("Skipping trip {} - outside time range", trip.getTripId());
                            return false;
                        }
                    }

                    // Check destination proximity
                    if (trip.getDestinationLocation() == null) {
                        return false;
                    }
                    double distance = calculateDistance(
                            destinationLatitude, destinationLongitude,
                            trip.getDestinationLocation().getY(),
                            trip.getDestinationLocation().getX());

                    if (distance > destRadiusInMeters) {
                        return false;
                    }

                    // Step 0.1 (continued): Check actual available seats
                    // availableSeats = totalSeats - bookedSeats
                    if (requestedSeats != null && requestedSeats > 0) {
                        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
                        if (availableSeats < requestedSeats) {
                            return false;
                        }
                    }

                    // Check minAvailableSeats filter
                    if (minAvailableSeats != null && minAvailableSeats > 0) {
                        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
                        if (availableSeats < minAvailableSeats) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();

        log.info(
                "Found {} trips matching route from ({}, {}) to ({}, {}) within {} km and {} km with {} requested seats for user {}",
                matchingTrips.size(), sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude,
                sourceRadiusKm, destRadiusKm,
                requestedSeats, effectiveUserId);

        return matchingTrips;
    }

    /**
     * Find trips within a specific area (bounding box)
     *
     * @param minLatitude  Minimum latitude
     * @param minLongitude Minimum longitude
     * @param maxLatitude  Maximum latitude
     * @param maxLongitude Maximum longitude
     * @return List of trips with source within the bounding box
     */
    public List<Trip> findTripsInArea(double minLatitude, double minLongitude, double maxLatitude,
            double maxLongitude) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sourceLocation")
                .within(new org.springframework.data.mongodb.core.geo.GeoJsonPolygon(
                        new Point(minLongitude, minLatitude),
                        new Point(maxLongitude, minLatitude),
                        new Point(maxLongitude, maxLatitude),
                        new Point(minLongitude, maxLatitude),
                        new Point(minLongitude, minLatitude))));
        query.addCriteria(new Criteria().orOperator(
                // TODO: Make this like Trip.status (no raw string)
                Criteria.where("tripStatus").is(TripStatus.CREATED),
                Criteria.where("tripStatus").is(TripStatus.AVAILABLE)));

        List<Trip> trips = mongoTemplate.find(query, Trip.class);
        log.info("Found {} trips in area ({}, {}) to ({}, {})",
                trips.size(), minLatitude, minLongitude, maxLatitude, maxLongitude);

        return trips;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * 
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

    /**
     * Parse datetime string to Instant
     * Supports both ISO 8601 with timezone and without timezone
     *
     * @param dateTimeStr DateTime string to parse
     * @return Instant representation
     */
    private Instant parseDateTime(String dateTimeStr) {
        return parseZonedDateTime(dateTimeStr).toInstant();
    }

    /**
     * Parse datetime string to ZonedDateTime
     * Supports both ISO 8601 with timezone and without timezone
     *
     * @param dateTimeStr DateTime string to parse
     * @return ZonedDateTime representation
     */
    private ZonedDateTime parseZonedDateTime(String dateTimeStr) {
        if (dateTimeStr.contains("+") || dateTimeStr.contains("Z")) {
            // Has timezone info
            return ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else {
            // No timezone, assume system timezone
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr);
            // TODO: Consider allowing client to specify default timezone instead of system
            // default
            // TODO: System represents everything in UTC internally or provides timezone
            // context
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            log.debug("No timezone in input '{}', using system default: {}", dateTimeStr, ZoneId.systemDefault());
            return zonedDateTime;
        }
    }
}
