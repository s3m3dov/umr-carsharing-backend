package com.cimhub.tripservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.testsupport.Points;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.request.TripSearchCriteria;
import com.cimhub.tripservice.dto.response.CompleteTripResponse;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.dto.response.StartTripResponse;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.model.OperationMessage;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.booking.CompletionService;
import com.cimhub.tripservice.service.geo.TripGeospatialService;
import com.cimhub.tripservice.service.trip.ActiveRidesService;
import com.cimhub.tripservice.service.trip.OfferRideService;
import com.cimhub.tripservice.service.trip.TripService;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
        tripController = new TripController(
                offerRideService, cancellationService, completionService, geoService, activeRidesService, tripService);
    }

    @Test
    void offerTrip_shouldReturnCreatedResponse() {
        OfferRideRequest request = OfferRideRequest.builder()
                .sourceAddress(Points.of(0, 0, ""))
                .destinationAddress(Points.of(0, 0, ""))
                .tripStartDateTime(ZonedDateTime.parse("2026-04-01T12:00:00Z"))
                .totalSeats(3)
                .pricePerSeat(5.0)
                .build();

        OfferRideResponse offerResponse =
                OfferRideResponse.builder().tripId("trip-1").tripCreated(true).build();
        when(offerRideService.offerRide("driver-1", request)).thenReturn(offerResponse);

        ResponseEntity<ApiResponseWrapper<OfferRideResponse>> response =
                tripController.offerTrip("driver-1", "DRIVER", request);

        assertEquals("trip-1", response.getBody().getData().tripId());
        verify(offerRideService).offerRide("driver-1", request);
    }

    @Test
    void cancelTrip_shouldReturnOkResponse() {
        CancelTripRequest request = CancelTripRequest.builder().tripId("trip-1").build();
        when(cancellationService.cancelTrip("driver-1", request)).thenReturn("Cancelled");

        ResponseEntity<ApiResponseWrapper<String>> response = tripController.cancelTrip("driver-1", "DRIVER", request);

        assertEquals("Cancelled", response.getBody().getMessage());
        verify(cancellationService).cancelTrip("driver-1", request);
    }

    @Test
    void getTrip_shouldReturnOkWithTripData() {
        TripResponse tripResponse = TripResponse.builder()
                .tripId("trip-1")
                .tripStatus(TripStatus.COMPLETED)
                .build();
        when(tripService.getTrip("trip-1")).thenReturn(tripResponse);

        ResponseEntity<ApiResponseWrapper<TripResponse>> response = tripController.getTrip("trip-1");

        assertEquals("trip-1", response.getBody().getData().tripId());
        assertEquals(TripStatus.COMPLETED, response.getBody().getData().tripStatus());
        verify(tripService).getTrip("trip-1");
    }

    @Test
    void searchEndpoints_shouldReturnGeoResults() {
        Trip trip = new Trip();
        trip.setTripId("trip-geo");

        when(geoService.findTripsWithSourceNear(1.1, 2.2, 5.0)).thenReturn(List.of(trip));
        when(geoService.findTripsWithDestinationNear(3.3, 4.4, 6.0)).thenReturn(List.of(trip));
        when(geoService.findTripsMatchingRoute(any(TripSearchCriteria.class))).thenReturn(List.of(trip));

        ResponseEntity<ApiResponseWrapper<List<Trip>>> source = tripController.searchNearSource(1.1, 2.2, 5.0);
        ResponseEntity<ApiResponseWrapper<List<Trip>>> destination =
                tripController.searchNearDestination(3.3, 4.4, 6.0);
        ResponseEntity<ApiResponseWrapper<List<Trip>>> route = tripController.searchMatchingRoute(
                "user-1",
                "PASSENGER",
                1.0,
                2.0,
                5.0,
                3.0,
                4.0,
                6.0,
                "2026-04-01T10:00:00Z",
                "2026-04-01T12:00:00Z",
                2,
                5.0,
                20.0,
                "SEDAN",
                1);

        assertEquals("trip-geo", source.getBody().getData().getFirst().getTripId());
        assertEquals("trip-geo", destination.getBody().getData().getFirst().getTripId());
        assertEquals("trip-geo", route.getBody().getData().getFirst().getTripId());
    }

    @Test
    void searchMatchingRoute_withoutTimeRange_shouldInvokeGeoServiceWithNullTimes() {
        Trip trip = new Trip();
        trip.setTripId("trip-no-time");

        when(geoService.findTripsMatchingRoute(any(TripSearchCriteria.class))).thenReturn(List.of(trip));

        ResponseEntity<ApiResponseWrapper<List<Trip>>> response = tripController.searchMatchingRoute(
                "user-2", "PASSENGER", 1.0, 2.0, 5.0, 3.0, 4.0, 6.0, null, null, null, null, null, null, null);

        assertEquals("trip-no-time", response.getBody().getData().getFirst().getTripId());
    }

    @Test
    void completeTrip_shouldReturnOkWithServiceResult() {
        CompleteTripResponse completeTripResponse = CompleteTripResponse.builder()
                .tripId("trip-1")
                .driverId("driver-1")
                .tripStatus(TripStatus.COMPLETED)
                .autoCompletedRides(2)
                .build();
        when(completionService.completeTrip("trip-1", "driver-1")).thenReturn(completeTripResponse);

        ResponseEntity<ApiResponseWrapper<CompleteTripResponse>> response =
                tripController.completeTrip("trip-1", "driver-1", "DRIVER");

        assertEquals(
                OperationMessage.TRIP_COMPLETED_SUCCESS.format(2),
                response.getBody().getMessage());
        assertEquals(TripStatus.COMPLETED, response.getBody().getData().tripStatus());
        assertEquals(2, response.getBody().getData().autoCompletedRides());
        verify(completionService).completeTrip("trip-1", "driver-1");
    }

    @Test
    void driverTripEndpoints_shouldReturnActiveAndHistoryLists() {
        DriverTripResponse active =
                DriverTripResponse.builder().tripId("active-trip").build();
        DriverTripResponse history =
                DriverTripResponse.builder().tripId("history-trip").build();

        when(activeRidesService.findDriverActiveTrips("driver-77")).thenReturn(List.of(active));
        when(activeRidesService.findDriverTripHistory("driver-77")).thenReturn(List.of(history));

        ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> activeResponse =
                tripController.getDriverActiveTrips("driver-77", "DRIVER", "driver-77");
        ResponseEntity<ApiResponseWrapper<List<DriverTripResponse>>> historyResponse =
                tripController.getDriverTripHistory("driver-77", "DRIVER", "driver-77");

        assertEquals(
                "active-trip", activeResponse.getBody().getData().getFirst().tripId());
        assertEquals(
                "history-trip", historyResponse.getBody().getData().getFirst().tripId());

        verify(activeRidesService).findDriverActiveTrips("driver-77");
        verify(activeRidesService).findDriverTripHistory("driver-77");
    }

    @Test
    void startTrip_shouldReturnOkWithServiceResult() {
        StartTripResponse startTripResponse = StartTripResponse.builder()
                .tripId("trip-1")
                .driverId("driver-1")
                .tripStatus(TripStatus.IN_PROGRESS)
                .build();
        when(tripService.startTrip("trip-1", "driver-1")).thenReturn(startTripResponse);

        ResponseEntity<ApiResponseWrapper<StartTripResponse>> response =
                tripController.startTrip("trip-1", "driver-1", "DRIVER");

        assertEquals(
                OperationMessage.TRIP_STARTED_SUCCESS.text(), response.getBody().getMessage());
        assertEquals(TripStatus.IN_PROGRESS, response.getBody().getData().tripStatus());
        verify(tripService).startTrip("trip-1", "driver-1");
    }

    @Test
    void startTrip_whenRoleIsNotDriver_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> tripController.startTrip("trip-1", "driver-1", "PASSENGER"));
    }

    @Test
    void completeTrip_whenRoleIsNotDriver_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> tripController.completeTrip("trip-1", "driver-1", "PASSENGER"));
    }
}
