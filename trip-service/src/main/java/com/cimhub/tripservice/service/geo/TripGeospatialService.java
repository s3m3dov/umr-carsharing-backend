package com.cimhub.tripservice.service.geo;

import com.cimhub.tripservice.dto.osrm.Geometry;
import com.cimhub.tripservice.dto.request.TripSearchCriteria;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.common.enums.TripStatus;
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
 * Service for geospatial queries on trips.
 * Allows searching for trips near a location or matching a route.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripGeospatialService {

    private static final int EARTH_RADIUS_METERS = 6371000;

    private final MongoTemplate mongoTemplate;

    public void enrichTripWithGeospatialData(Trip trip, com.cimhub.tripservice.dto.osrm.Route route) {
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
     * Find trips with source location near the given coordinates.
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
     * Find trips with destination location near the given coordinates.
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
     * Find trips matching both source and destination within given radius.
     * Note: MongoDB doesn't allow multiple $geoNear operations in one query,
     * so we search by source first, then filter by destination in memory.
     *
     * @param criteria all search parameters encapsulated in {@link TripSearchCriteria}
     * @return List of matching trips
     */
    public List<Trip> findTripsMatchingRoute(TripSearchCriteria criteria) {
        Instant searchWindowStart = null;
        Instant searchWindowEnd = null;

        if (criteria.earliestDepartureTime() != null || criteria.latestDepartureTime() != null) {
            try {
                if (criteria.earliestDepartureTime() != null) {
                    searchWindowStart = parseDateTime(criteria.earliestDepartureTime());
                    log.info("Earliest departure time: {}", searchWindowStart);
                }
                if (criteria.latestDepartureTime() != null) {
                    searchWindowEnd = parseDateTime(criteria.latestDepartureTime());
                    log.info("Latest departure time: {}", searchWindowEnd);
                }

                if (searchWindowStart != null && searchWindowEnd != null
                        && !searchWindowStart.isBefore(searchWindowEnd)) {
                    throw new IllegalArgumentException(
                            "latestDepartureTime must be after earliestDepartureTime");
                }

                log.info("Using time range filter: {} to {}", searchWindowStart, searchWindowEnd);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to parse time range parameters. Error: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid time range parameters: " + e.getMessage());
            }
        } else {
            searchWindowStart = Instant.now();
            log.info("No time parameters provided. Searching all future trips from: {}", searchWindowStart);
        }

        final Instant finalSearchWindowStart = searchWindowStart;
        final Instant finalSearchWindowEnd = searchWindowEnd;

        Point sourceLocation = new Point(criteria.sourceLongitude(), criteria.sourceLatitude());
        Distance sourceDistance = new Distance(criteria.sourceRadiusKm(), Metrics.KILOMETERS);

        Query query = new Query();
        query.addCriteria(Criteria.where("sourceLocation")
                .nearSphere(sourceLocation)
                .maxDistance(sourceDistance.getNormalizedValue()));
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("tripStatus").is(TripStatus.CREATED),
                Criteria.where("tripStatus").is(TripStatus.AVAILABLE)));

        if (searchWindowStart != null && searchWindowEnd != null) {
            query.addCriteria(Criteria.where("tripStartDateTimeUTC")
                    .gte(searchWindowStart).lt(searchWindowEnd).ne(null));
        } else if (searchWindowStart != null) {
            query.addCriteria(Criteria.where("tripStartDateTimeUTC").gte(searchWindowStart).ne(null));
        } else if (searchWindowEnd != null) {
            query.addCriteria(Criteria.where("tripStartDateTimeUTC").lt(searchWindowEnd).ne(null));
        } else {
            query.addCriteria(Criteria.where("tripStartDateTimeUTC").ne(null));
        }

        if (criteria.requestedSeats() != null && criteria.requestedSeats() > 0) {
            query.addCriteria(Criteria.where("totalSeats").gte(criteria.requestedSeats()));
        }

        if (criteria.excludeDriverId() != null && !criteria.excludeDriverId().isEmpty()) {
            query.addCriteria(Criteria.where("driverId").ne(criteria.excludeDriverId()));
        }

        if (criteria.minPrice() != null && criteria.maxPrice() != null) {
            query.addCriteria(Criteria.where("pricePerSeat").gte(criteria.minPrice()).lte(criteria.maxPrice()));
        } else if (criteria.minPrice() != null) {
            query.addCriteria(Criteria.where("pricePerSeat").gte(criteria.minPrice()));
        } else if (criteria.maxPrice() != null) {
            query.addCriteria(Criteria.where("pricePerSeat").lte(criteria.maxPrice()));
        }

        if (criteria.carType() != null && !criteria.carType().isEmpty()) {
            query.addCriteria(Criteria.where("carType").is(criteria.carType()));
        }

        List<Trip> tripsNearSource = mongoTemplate.find(query, Trip.class);
        log.info("Found {} trips near source ({}, {}) within {} km after initial filtering",
                tripsNearSource.size(), criteria.sourceLatitude(), criteria.sourceLongitude(), criteria.sourceRadiusKm());

        double destRadiusInMeters = criteria.destinationRadiusKm() * 1000;

        List<Trip> matchingTrips = tripsNearSource.stream()
                .filter(trip -> {
                    if (trip.getTripStartDateTimeUTC() == null) {
                        log.warn("Skipping trip {} - missing tripStartDateTimeUTC", trip.getTripId());
                        return false;
                    }

                    if (finalSearchWindowStart != null && finalSearchWindowEnd != null) {
                        Instant tripTime = trip.getTripStartDateTimeUTC();
                        if (tripTime.isBefore(finalSearchWindowStart) || !tripTime.isBefore(finalSearchWindowEnd)) {
                            log.debug("Skipping trip {} - outside time range", trip.getTripId());
                            return false;
                        }
                    }

                    if (trip.getDestinationLocation() == null) {
                        return false;
                    }
                    double distance = calculateDistance(
                            criteria.destinationLatitude(), criteria.destinationLongitude(),
                            trip.getDestinationLocation().getY(),
                            trip.getDestinationLocation().getX());

                    if (distance > destRadiusInMeters) {
                        return false;
                    }

                    if (criteria.requestedSeats() != null && criteria.requestedSeats() > 0) {
                        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
                        if (availableSeats < criteria.requestedSeats()) {
                            return false;
                        }
                    }

                    if (criteria.minAvailableSeats() != null && criteria.minAvailableSeats() > 0) {
                        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
                        if (availableSeats < criteria.minAvailableSeats()) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();

        log.info("Found {} trips matching route from ({}, {}) to ({}, {}) within {} km and {} km",
                matchingTrips.size(),
                criteria.sourceLatitude(), criteria.sourceLongitude(),
                criteria.destinationLatitude(), criteria.destinationLongitude(),
                criteria.sourceRadiusKm(), criteria.destinationRadiusKm());

        return matchingTrips;
    }

    /**
     * Find trips within a specific area (bounding box).
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
                Criteria.where("tripStatus").is(TripStatus.CREATED),
                Criteria.where("tripStatus").is(TripStatus.AVAILABLE)));

        List<Trip> trips = mongoTemplate.find(query, Trip.class);
        log.info("Found {} trips in area ({}, {}) to ({}, {})",
                trips.size(), minLatitude, minLongitude, maxLatitude, maxLongitude);

        return trips;
    }

    /**
     * Haversine formula to calculate distance between two coordinates.
     *
     * @return distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Parse datetime string to Instant.
     * Supports ISO 8601 with or without timezone offset.
     * When no timezone is present, system default is used.
     */
    private Instant parseDateTime(String dateTimeStr) {
        return parseZonedDateTime(dateTimeStr).toInstant();
    }

    private ZonedDateTime parseZonedDateTime(String dateTimeStr) {
        if (dateTimeStr.contains("+") || dateTimeStr.contains("Z")) {
            return ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr);
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            log.debug("No timezone in input '{}', using system default: {}", dateTimeStr, ZoneId.systemDefault());
            return zonedDateTime;
        }
    }
}
