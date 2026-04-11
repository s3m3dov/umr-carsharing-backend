package com.cimhub.tripservice.service.trip;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.osrm.Geometry;
import com.cimhub.tripservice.dto.osrm.OsrmResponseDTO;
import com.cimhub.tripservice.dto.osrm.Route;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.tripservice.model.VehicleDocument;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.repository.VehicleDocumentRepository;
import com.cimhub.tripservice.service.geo.TripGeospatialService;
import com.cimhub.tripservice.service.route.OsrmRouteClient;
import com.cimhub.tripservice.testsupport.TripFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferRideServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private VehicleDocumentRepository vehicleDocumentRepository;

    @Mock
    private OsrmRouteClient osmRoute;

    @Mock
    private TripGeospatialService tripGeospatialService;

    @Mock
    private AuditService auditService;

    private OfferRideService offerRideService;

    @BeforeEach
    void setUp() {
        offerRideService = new OfferRideService(tripsRepository, vehicleDocumentRepository, osmRoute, tripGeospatialService, auditService);
    }

    @Test
    void offerRide_whenRouteResolved_shouldCreateTripAndReturnResponse() {
        OfferRideRequest request = TripFixtures.offerRideRequest();
        Route route = new Route(
                new Geometry("LineString", List.of(List.of(13.4, 52.5), List.of(13.5, 52.6))),
                List.of(),
                12000.0,
                1800.0,
                "routability",
                1.0);
        OsrmResponseDTO osrmResponse = new OsrmResponseDTO("Ok", List.of(route), List.of());
        VehicleDocument vehicleDoc = new VehicleDocument();
        vehicleDoc.setUserId("driver-1");
        vehicleDoc.setVehicleNumber("HE-XX-1234");
        vehicleDoc.setVehicleType("SEDAN");

        when(osmRoute.getRoute(52.5, 13.4, 52.6, 13.5)).thenReturn(Mono.just(osrmResponse));
        when(vehicleDocumentRepository.findByUserId("driver-1"))
                .thenReturn(Optional.of(vehicleDoc));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip saved = invocation.getArgument(0);
            saved.setTripId("trip-42");
            return saved;
        });

        OfferRideResponse response = offerRideService.offerRide(request);

        assertTrue(response.tripCreated());
        assertEquals("trip-42", response.tripId());
        assertEquals(12000.0, response.routeDistanceInMeters());
        assertEquals(12.0, response.routeDistanceInKm());
        assertEquals(1800.0, response.routeDurationInSeconds());
        assertEquals(30.0, response.routeDurationInMinutes());
        assertEquals("UTC", response.tripTimezone());
        assertEquals(5.0, response.pricePerSeat());

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();

        assertEquals("driver-1", savedTrip.getDriverId());
        assertEquals("HE-XX-1234", savedTrip.getVehicleNumber());
        assertEquals("SEDAN", savedTrip.getCarType());
        assertEquals(5.0, savedTrip.getPricePerSeat());
        assertEquals(TripStatus.AVAILABLE, savedTrip.getTripStatus());
        assertEquals(0, savedTrip.getBookedSeats());
        assertEquals(Instant.parse("2026-05-01T10:15:30Z"), savedTrip.getTripStartDateTimeUTC());

        verify(tripGeospatialService).enrichTripWithGeospatialData(any(Trip.class), eq(route));
        verify(auditService).logCreate(EntityType.TRIP, "trip-42", "driver-1", "Trip created by driver");
    }

    @Test
    void offerRide_whenRouteClientReturnsNull_shouldReturnFailureResponse() {
        OfferRideRequest request = TripFixtures.offerRideRequest();
        when(osmRoute.getRoute(52.5, 13.4, 52.6, 13.5)).thenReturn(Mono.empty());

        OfferRideResponse response = offerRideService.offerRide(request);

        assertFalse(response.tripCreated());
        assertEquals("Could not calculate route for provided coordinates", response.errorMessage());
        verify(tripsRepository, never()).save(any(Trip.class));
        verifyNoInteractions(tripGeospatialService, auditService);
    }

    @Test
    void offerRide_whenRouteClientReturnsEmptyRoutes_shouldReturnFailureResponse() {
        OfferRideRequest request = TripFixtures.offerRideRequest();
        OsrmResponseDTO osrmResponse = new OsrmResponseDTO("Ok", List.of(), List.of());
        when(osmRoute.getRoute(52.5, 13.4, 52.6, 13.5)).thenReturn(Mono.just(osrmResponse));

        OfferRideResponse response = offerRideService.offerRide(request);

        assertFalse(response.tripCreated());
        assertEquals("Could not calculate route for provided coordinates", response.errorMessage());
        verify(tripsRepository, never()).save(any(Trip.class));
        verifyNoInteractions(tripGeospatialService, auditService);
    }

    @Test
    void offerRide_whenVehicleNotFound_shouldReturnFailureResponse() {
        OfferRideRequest request = TripFixtures.offerRideRequest();
        OsrmResponseDTO osrmResponse = new OsrmResponseDTO("Ok", List.of(
                new Route(new Geometry("LineString", List.of()), List.of(), 1000.0, 60.0, "routability", 1.0)),
                List.of());

        when(osmRoute.getRoute(52.5, 13.4, 52.6, 13.5)).thenReturn(Mono.just(osrmResponse));
        when(vehicleDocumentRepository.findByUserId("driver-1"))
                .thenReturn(Optional.empty());

        OfferRideResponse response = offerRideService.offerRide(request);

        assertFalse(response.tripCreated());
        assertEquals("Vehicle not found or does not belong to driver", response.errorMessage());
        verify(tripsRepository, never()).save(any(Trip.class));
        verifyNoInteractions(tripGeospatialService, auditService);
    }

    @Test
    void offerRide_withZeroPricePerSeat_shouldCreateFreeRide() {
        OfferRideRequest baseRequest = TripFixtures.offerRideRequest();
        OfferRideRequest request = OfferRideRequest.builder()
                .driverId("driver-1")
                .sourceAddress(baseRequest.sourceAddress())
                .destinationAddress(baseRequest.destinationAddress())
                .tripStartDateTime(ZonedDateTime.parse("2026-05-01T10:15:30.987Z"))
                .totalSeats(3)
                .pricePerSeat(0.0)
                .build();
        Route route = new Route(
                new Geometry("LineString", List.of()),
                List.of(), 1000.0, 60.0, "routability", 1.0);
        OsrmResponseDTO osrmResponse = new OsrmResponseDTO("Ok", List.of(route), List.of());
        VehicleDocument vehicleDoc = new VehicleDocument();
        vehicleDoc.setVehicleType("SEDAN");

        when(osmRoute.getRoute(52.5, 13.4, 52.6, 13.5)).thenReturn(Mono.just(osrmResponse));
        when(vehicleDocumentRepository.findByUserId("driver-1"))
                .thenReturn(Optional.of(vehicleDoc));
        when(tripsRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OfferRideResponse response = offerRideService.offerRide(request);

        assertTrue(response.tripCreated());
        assertEquals(0.0, response.pricePerSeat());
    }
}
