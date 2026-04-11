package com.cimhub.tripservice.service.geo;

import com.cimhub.common.model.Points;
import com.cimhub.tripservice.dto.osrm.Geometry;
import com.cimhub.tripservice.dto.osrm.Route;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.dto.request.TripSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripGeospatialServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private TripGeospatialService tripGeospatialService;

    @BeforeEach
    void setUp() {
        tripGeospatialService = new TripGeospatialService(mongoTemplate);
    }

    @Test
    void enrichTripWithGeospatialData_shouldSetRouteAndGeoPoints() {
        Trip trip = new Trip();
        Points source = com.cimhub.common.testsupport.Points.of(52.5, 13.4, "Source");
        Points destination = com.cimhub.common.testsupport.Points.of(52.6, 13.5, "Destination");
        trip.setSourceAddress(source);
        trip.setDestinationAddress(destination);

        Route route = new Route(
                new Geometry("LineString", List.of(List.of(13.4, 52.5), List.of(13.5, 52.6))),
                List.of(),
                12345.0,
                2100.0,
                "routability",
                1.0);

        tripGeospatialService.enrichTripWithGeospatialData(trip, route);

        assertNotNull(trip.getRouteGeometry());
        assertEquals(12345.0, trip.getRouteDistance());
        assertEquals(2100.0, trip.getRouteDuration());
        assertEquals(13.4, trip.getSourceLocation().getX());
        assertEquals(52.5, trip.getSourceLocation().getY());
        assertEquals(13.5, trip.getDestinationLocation().getX());
        assertEquals(52.6, trip.getDestinationLocation().getY());
    }

    @Test
    void enrichTripWithGeospatialData_whenTripOrRouteNull_shouldReturnWithoutFailure() {
        Trip trip = new Trip();

        tripGeospatialService.enrichTripWithGeospatialData(null, null);
        tripGeospatialService.enrichTripWithGeospatialData(trip, null);

        assertEquals(0.0, trip.getRouteDistance());
        assertEquals(0.0, trip.getRouteDuration());
    }

    @Test
    void findTripsWithSourceNear_shouldQueryMongoAndReturnTrips() {
        Trip trip = new Trip();
        trip.setTripId("trip-source");
        when(mongoTemplate.find(any(Query.class), eq(Trip.class))).thenReturn(List.of(trip));

        List<Trip> result = tripGeospatialService.findTripsWithSourceNear(52.5, 13.4, 10.0);

        assertEquals(1, result.size());
        assertEquals("trip-source", result.getFirst().getTripId());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Trip.class));
        assertTrue(queryCaptor.getValue().getQueryObject().containsKey("sourceLocation"));
    }

    @Test
    void findTripsWithDestinationNear_shouldQueryMongoAndReturnTrips() {
        Trip trip = new Trip();
        trip.setTripId("trip-dest");
        when(mongoTemplate.find(any(Query.class), eq(Trip.class))).thenReturn(List.of(trip));

        List<Trip> result = tripGeospatialService.findTripsWithDestinationNear(52.6, 13.5, 8.0);

        assertEquals(1, result.size());
        assertEquals("trip-dest", result.getFirst().getTripId());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Trip.class));
        assertTrue(queryCaptor.getValue().getQueryObject().containsKey("destinationLocation"));
    }

    @Test
    void findTripsMatchingRoute_shouldFilterByTimeDistanceAndSeats() {
        Trip missingTime = trip("missing-time", null, new Point(13.5002, 52.6002), 5, 0);
        Trip outsideRange = trip("outside-range", Instant.parse("2026-06-01T12:00:00Z"), new Point(13.5002, 52.6002), 5, 0);
        Trip noDestination = trip("no-destination", Instant.parse("2026-06-01T11:00:00Z"), null, 5, 0);
        Trip farAway = trip("far", Instant.parse("2026-06-01T11:00:00Z"), new Point(0.0, 0.0), 5, 0);
        Trip insufficientRequested = trip("insufficient-requested", Instant.parse("2026-06-01T11:00:00Z"), new Point(13.5002, 52.6002), 1, 1);
        Trip insufficientMinAvailable = trip("insufficient-min-available", Instant.parse("2026-06-01T11:00:00Z"), new Point(13.5002, 52.6002), 3, 1);
        Trip passing = trip("ok", Instant.parse("2026-06-01T11:00:00Z"), new Point(13.5003, 52.6003), 6, 1);

        when(mongoTemplate.find(any(Query.class), eq(Trip.class))).thenReturn(List.of(
                missingTime,
                outsideRange,
                noDestination,
                farAway,
                insufficientRequested,
                insufficientMinAvailable,
                passing));

        List<Trip> result = tripGeospatialService.findTripsMatchingRoute(
                new TripSearchCriteria(
                        52.5, 13.4, 10.0,
                        52.6, 13.5, 5.0,
                        "2026-06-01T10:00:00Z",
                        "2026-06-01T12:00:00Z",
                        1,
                        "current-user",
                        10.0,
                        30.0,
                        "SEDAN",
                        4));

        assertEquals(1, result.size());
        assertEquals("ok", result.getFirst().getTripId());
    }

    @Test
    void findTripsMatchingRoute_whenInvalidTimeRange_shouldThrow() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tripGeospatialService.findTripsMatchingRoute(
                        new TripSearchCriteria(
                                52.5, 13.4, 5.0,
                                52.6, 13.5, 5.0,
                                "2026-06-01T12:00:00Z",
                                "2026-06-01T11:00:00Z",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null)));

        assertEquals("latestDepartureTime must be after earliestDepartureTime", ex.getMessage());
    }

    @Test
    void findTripsMatchingRoute_whenInvalidTimeFormat_shouldThrow() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tripGeospatialService.findTripsMatchingRoute(
                        new TripSearchCriteria(
                                52.5, 13.4, 5.0,
                                52.6, 13.5, 5.0,
                                "not-a-date",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null)));

        assertTrue(ex.getMessage().startsWith("Invalid time range parameters:"));
    }

    @Test
    void findTripsMatchingRoute_shouldHandleNoTimeOnlyEarliestAndOnlyLatestBranches() {
        Trip passing = trip("ok", Instant.parse("2026-07-01T11:00:00Z"), new Point(13.5003, 52.6003), 4, 0);
        when(mongoTemplate.find(any(Query.class), eq(Trip.class))).thenReturn(List.of(passing));

        List<Trip> noTime = tripGeospatialService.findTripsMatchingRoute(
                new TripSearchCriteria(
                        52.5, 13.4, 10.0,
                        52.6, 13.5, 5.0,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        List<Trip> onlyEarliest = tripGeospatialService.findTripsMatchingRoute(
                new TripSearchCriteria(
                        52.5, 13.4, 10.0,
                        52.6, 13.5, 5.0,
                        "2026-06-01T09:00:00",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        List<Trip> onlyLatest = tripGeospatialService.findTripsMatchingRoute(
                new TripSearchCriteria(
                        52.5, 13.4, 10.0,
                        52.6, 13.5, 5.0,
                        null,
                        "2026-06-01T14:00:00Z",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        assertEquals(1, noTime.size());
        assertEquals(1, onlyEarliest.size());
        assertEquals(1, onlyLatest.size());
    }

    @Test
    void findTripsInArea_shouldQueryMongoAndReturnTrips() {
        Trip trip = new Trip();
        trip.setTripId("trip-area");
        when(mongoTemplate.find(any(Query.class), eq(Trip.class))).thenReturn(List.of(trip));

        List<Trip> result = tripGeospatialService.findTripsInArea(52.4, 13.3, 52.7, 13.6);

        assertEquals(1, result.size());
        assertEquals("trip-area", result.getFirst().getTripId());
    }

    private Trip trip(String id, Instant startTime, Point destination, int totalSeats, int bookedSeats) {
        Trip trip = new Trip();
        trip.setTripId(id);
        trip.setTripStartDateTimeUTC(startTime);
        trip.setDestinationLocation(destination);
        trip.setTotalSeats(totalSeats);
        trip.setBookedSeats(bookedSeats);
        trip.setDriverId("driver-x");
        trip.setCarType("SEDAN");
        trip.setPricePerSeat(15.0);
        return trip;
    }
}
