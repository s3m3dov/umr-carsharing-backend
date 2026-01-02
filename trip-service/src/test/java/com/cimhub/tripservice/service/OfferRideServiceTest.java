package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.OSRM.Geometry;
import com.cimhub.tripservice.dto.OSRM.OsrmResponseDTO;
import com.cimhub.tripservice.dto.OSRM.Route;
import com.cimhub.tripservice.dto.common.LocationPoint;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.routes.OSMRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferRideServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private OSMRoute osmRoute;

    @Mock
    private TripGeospatialService tripGeospatialService;

    @InjectMocks
    private OfferRideService offerRideService;

    private OfferRideRequest offerRideRequest;
    private OsrmResponseDTO mockOsrmResponse;
    private Route mockRoute;

    @BeforeEach
    void setUp() {
        LocationPoint source = new LocationPoint(40.7128, -74.0060, "New York");
        LocationPoint destination = new LocationPoint(34.0522, -118.2437, "Los Angeles");

        offerRideRequest = new OfferRideRequest(
                "driver123",
                "ABC123",
                source,
                destination,
                ZonedDateTime.now().plusDays(1),
                4);

        Geometry mockGeometry = new Geometry("encoded_polyline_string");
        mockRoute = new Route(
                mockGeometry,
                Collections.emptyList(),
                4500000.0, // distance in meters
                162000.0 // duration in seconds
        );

        mockOsrmResponse = new OsrmResponseDTO(
                "Ok",
                List.of(mockRoute),
                Collections.emptyList());
    }

    @Test
    void offerRide_whenValidRequest_thenSuccessfullyCreatesTrip() {
        // Given
        when(osmRoute.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Mono.just(mockOsrmResponse));
        doNothing().when(tripGeospatialService).enrichTripWithGeospatialData(any(Trip.class), any(Route.class));

        // When
        OfferRideResponse response = offerRideService.offerRide(offerRideRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tripCreated()).isTrue();
        assertThat(response.errorMessage()).isNull();
        assertThat(response.vehicleNumber()).isEqualTo("ABC123");
        assertThat(response.routeDistanceInMeters()).isEqualTo(4500000.0);
        assertThat(response.routeDistanceInKm()).isEqualTo(4500.0);
        assertThat(response.routeDurationInSeconds()).isEqualTo(162000.0);
        assertThat(response.routeDurationInMinutes()).isEqualTo(2700.0);

        verify(tripsRepository).save(any(Trip.class));
        verify(tripGeospatialService).enrichTripWithGeospatialData(any(Trip.class), eq(mockRoute));
    }

    @Test
    void offerRide_whenRouteCalculationFails_thenReturnsErrorResponse() {
        // Given
        when(osmRoute.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Mono.just(new OsrmResponseDTO("Ok", Collections.emptyList(), Collections.emptyList())));

        // When
        OfferRideResponse response = offerRideService.offerRide(offerRideRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tripCreated()).isFalse();
        assertThat(response.errorMessage()).isEqualTo("Could not calculate route for provided coordinates");

        verify(tripsRepository, never()).save(any(Trip.class));
        verify(tripGeospatialService, never()).enrichTripWithGeospatialData(any(Trip.class), any(Route.class));
    }

    @Test
    void offerRide_whenOsrmReturnsNull_thenReturnsErrorResponse() {
        // Given
        when(osmRoute.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Mono.empty());

        // When
        OfferRideResponse response = offerRideService.offerRide(offerRideRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tripCreated()).isFalse();
        assertThat(response.errorMessage()).contains("Could not calculate route");

        verify(tripsRepository, never()).save(any(Trip.class));
    }

    @Test
    void offerRide_whenCalled_thenSetsCorrectTripProperties() {
        // Given
        when(osmRoute.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Mono.just(mockOsrmResponse));
        doNothing().when(tripGeospatialService).enrichTripWithGeospatialData(any(Trip.class), any(Route.class));

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);

        // When
        offerRideService.offerRide(offerRideRequest);

        // Then
        verify(tripsRepository).save(tripCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();

        assertThat(savedTrip.getDriverId()).isEqualTo("driver123");
        assertThat(savedTrip.getVehicleNumber()).isEqualTo("ABC123");
        assertThat(savedTrip.getOfferedSeat()).isEqualTo(4);
        assertThat(savedTrip.getCurrSeats()).isEqualTo(0);
        assertThat(savedTrip.getTripStatus()).isEqualTo("AVAILABLE");
        assertThat(savedTrip.getSourceAddress()).isEqualTo(offerRideRequest.sourceAddress());
        assertThat(savedTrip.getDestinationAddress()).isEqualTo(offerRideRequest.destinationAddress());
        assertThat(savedTrip.getTripTimezone()).isNotNull();
    }

    @Test
    void offerRide_whenCalled_thenCallsOsmRouteWithCorrectCoordinates() {
        // Given
        when(osmRoute.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Mono.just(mockOsrmResponse));
        doNothing().when(tripGeospatialService).enrichTripWithGeospatialData(any(Trip.class), any(Route.class));

        // When
        offerRideService.offerRide(offerRideRequest);

        // Then
        verify(osmRoute).getRoute(
                eq(40.7128),
                eq(-74.0060),
                eq(34.0522),
                eq(-118.2437));
    }

    @Test
    void offerRide_whenSuccessful_thenEnrichesWithGeospatialData() {
        // Given
        when(osmRoute.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Mono.just(mockOsrmResponse));
        doNothing().when(tripGeospatialService).enrichTripWithGeospatialData(any(Trip.class), any(Route.class));

        // When
        offerRideService.offerRide(offerRideRequest);

        // Then
        verify(tripGeospatialService).enrichTripWithGeospatialData(any(Trip.class), eq(mockRoute));
    }
}
