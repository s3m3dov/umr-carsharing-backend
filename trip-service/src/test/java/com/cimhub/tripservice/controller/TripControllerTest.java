package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.models.Points;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.ActiveRidesService;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.CompletionService;
import com.cimhub.tripservice.service.OfferRideService;
import com.cimhub.tripservice.service.TripGeospatialService;
import com.cimhub.tripservice.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    @Mock
    private OfferRideService offerRideService;

    @Mock
    private CancellationService cancellationService;

    @Mock
    private CompletionService completionService;

    @Mock
    private TripGeospatialService geoService;

    @Mock
    private ActiveRidesService activeRidesService;

    @Mock
    private TripService tripService;

    private TripController tripController;

    @BeforeEach
    void setUp() {
        tripController = new TripController(offerRideService, cancellationService, completionService, geoService, activeRidesService, tripService);
    }

    @Test
    void offerTrip_shouldReturnCreatedResponse() {
        OfferRideRequest request = OfferRideRequest.builder()
                .driverId("driver-1")
                .sourceAddress(new Points())
                .destinationAddress(new Points())
                .tripStartDateTime(ZonedDateTime.parse("2026-04-01T12:00:00Z"))
                .totalSeats(3)
                .pricePerSeat(5.0)
                .build();

        OfferRideResponse offerResponse = OfferRideResponse.builder().tripId("trip-1").tripCreated(true).build();
        when(offerRideService.offerRide(request)).thenReturn(offerResponse);

        ResponseEntity<ApiResponseWrapper<OfferRideResponse>> response = tripController.offerTrip(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("trip-1", response.getBody().getData().tripId());
        verify(offerRideService).offerRide(request);
    }

    @Test
    void cancelTrip_shouldReturnOkResponse() {
        CancelTripRequest request = CancelTripRequest.builder().userId("driver-1").tripId("trip-1").build();
        when(cancellationService.cancelTrip(request)).thenReturn("Cancelled");

        ResponseEntity<ApiResponseWrapper<String>> response = tripController.cancelTrip(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cancelled", response.getBody().getMessage());
        verify(cancellationService).cancelTrip(request);
    }

    @Test
    void searchEndpoints_shouldReturnGeoResults() {
        Trip trip = new Trip();
        trip.setTripId("trip-geo");

        when(geoService.findTripsWithSourceNear(1.1, 2.2, 5.0)).thenReturn(List.of(trip));
        when(geoService.findTripsWithDestinationNear(3.3, 4.4, 6.0)).thenReturn(List.of(trip));
        when(geoService.findTripsMatchingRoute(
                1.0, 2.0, 5.0,
                3.0, 4.0, 6.0,
                "2026-04-01T10:00:00Z", "2026-04-01T12:00:00Z",
                2, "user-1",
                5.0, 20.0, "SEDAN", 1)).thenReturn(List.of(trip));

        ResponseEntity<ApiResponseWrapper<List<Trip>>> source = tripController.searchNearSource(1.1, 2.2, 5.0);
        ResponseEntity<ApiResponseWrapper<List<Trip>>> destination = tripController.searchNearDestination(3.3, 4.4, 6.0);
        ResponseEntity<ApiResponseWrapper<List<Trip>>> route = tripController.searchMatchingRoute(
                1.0, 2.0, 5.0,
                3.0, 4.0, 6.0,
                "2026-04-01T10:00:00Z", "2026-04-01T12:00:00Z",
                2, "user-1",
                5.0, 20.0, "SEDAN", 1);

        assertEquals(HttpStatus.OK, source.getStatusCode());
        assertEquals("trip-geo", source.getBody().getData().getFirst().getTripId());
        assertEquals(HttpStatus.OK, destination.getStatusCode());
        assertEquals(HttpStatus.OK, route.getStatusCode());
    }

    @Test
    void searchMatchingRoute_withoutTimeRange_shouldInvokeGeoServiceWithNullTimes() {
        Trip trip = new Trip();
        trip.setTripId("trip-no-time");

        when(geoService.findTripsMatchingRoute(
                1.0, 2.0, 5.0,
                3.0, 4.0, 6.0,
                null, null,
                null, "user-2",
                null, null, null, null)).thenReturn(List.of(trip));

        ResponseEntity<ApiResponseWrapper<List<Trip>>> response = tripController.searchMatchingRoute(
                1.0, 2.0, 5.0,
                3.0, 4.0, 6.0,
                null, null,
                null, "user-2",
                null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("trip-no-time", response.getBody().getData().getFirst().getTripId());
    }

    @Test
    void completeTrip_shouldReturnOkWithServiceResult() {
        when(completionService.completeTrip("trip-1", "driver-1"))
                .thenReturn("Trip completed successfully. 2 remaining ride(s) were auto-completed");

        ResponseEntity<ApiResponseWrapper<String>> response = tripController.completeTrip("trip-1", "driver-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Trip completed successfully. 2 remaining ride(s) were auto-completed",
                response.getBody().getMessage());
        verify(completionService).completeTrip("trip-1", "driver-1");
    }

    @Test
    void driverTripEndpoints_shouldReturnActiveAndHistoryLists() {
        DriverTripResponse active = DriverTripResponse.builder().tripId("active-trip").build();
        DriverTripResponse history = DriverTripResponse.builder().tripId("history-trip").build();

        when(activeRidesService.findDriverActiveTrips("driver-77")).thenReturn(List.of(active));
        when(activeRidesService.findDriverTripHistory("driver-77")).thenReturn(List.of(history));

        ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> activeResponse =
                tripController.getDriverActiveTrips("driver-77");
        ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> historyResponse =
                tripController.getDriverTripHistory("driver-77");

        assertEquals(HttpStatus.OK, activeResponse.getStatusCode());
        assertEquals("active-trip", activeResponse.getBody().getData().getFirst().tripId());
        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertEquals("history-trip", historyResponse.getBody().getData().getFirst().tripId());

        verify(activeRidesService).findDriverActiveTrips("driver-77");
        verify(activeRidesService).findDriverTripHistory("driver-77");
    }

    @Test
    void startTrip_shouldReturnOkWithServiceResult() {
        when(tripService.startTrip("trip-1", "driver-1")).thenReturn("Trip started successfully");

        ResponseEntity<ApiResponseWrapper<String>> response = tripController.startTrip("trip-1", "driver-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Trip started successfully", response.getBody().getMessage());
        verify(tripService).startTrip("trip-1", "driver-1");
    }
}
