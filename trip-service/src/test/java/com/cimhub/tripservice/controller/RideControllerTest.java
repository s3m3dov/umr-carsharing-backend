package com.cimhub.tripservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.booking.JoinTripService;
import com.cimhub.tripservice.service.trip.ActiveRidesService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RideControllerTest {

    @Mock
    private JoinTripService joinTripService;

    @Mock
    private CancellationService cancellationService;

    @Mock
    private ActiveRidesService activeRidesService;

    private RideController rideController;

    @BeforeEach
    void setUp() {
        rideController = new RideController(joinTripService, cancellationService, activeRidesService);
    }

    @Test
    void bookRide_shouldReturnCreatedWithServiceMessage() {
        JoinTripRequest request = JoinTripRequest.builder()
                .tripId("trip-1")
                .driverId("d-1")
                .requestedSeats(1)
                .build();
        when(joinTripService.joinTrip("p-1", request)).thenReturn("Ride booked");

        ResponseEntity<ApiResponseWrapper<String>> response = rideController.bookRide("p-1", "PASSENGER", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Ride booked", response.getBody().getMessage());
        verify(joinTripService).joinTrip("p-1", request);
    }

    @Test
    void bookRideWithDriverAccept_shouldReturnCreatedWithServiceMessage() {
        JoinTripRequest request = JoinTripRequest.builder()
                .tripId("trip-2")
                .driverId("d-2")
                .requestedSeats(2)
                .build();
        when(joinTripService.joinTripWithDriverAccept("p-2", request)).thenReturn("Booking requested");

        ResponseEntity<ApiResponseWrapper<String>> response =
                rideController.bookRideWithDriverAccept("p-2", "PASSENGER", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Booking requested", response.getBody().getMessage());
        verify(joinTripService).joinTripWithDriverAccept("p-2", request);
    }

    @Test
    void cancelRide_shouldReturnOkWithServiceMessage() {
        CancelTripRequest request = CancelTripRequest.builder().rideId("b-1").build();
        when(cancellationService.cancelRide("p-1", request)).thenReturn("Cancelled");

        ResponseEntity<ApiResponseWrapper<String>> response = rideController.cancelRide("p-1", "PASSENGER", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cancelled", response.getBody().getMessage());
        verify(cancellationService).cancelRide("p-1", request);
    }

    @Test
    void getPassengerActiveAndHistory_shouldReturnLists() {
        PassengerRideResponse active =
                PassengerRideResponse.builder().rideId("active-1").build();
        PassengerRideResponse history =
                PassengerRideResponse.builder().rideId("history-1").build();
        when(activeRidesService.findPassengerActiveRides("p-10")).thenReturn(List.of(active));
        when(activeRidesService.findPassengerRideHistory("p-10")).thenReturn(List.of(history));

        ResponseEntity<ApiResponseWrapper<List<PassengerRideResponse>>> activeResponse =
                rideController.getPassengerActiveRides("p-10", "PASSENGER", "p-10");
        ResponseEntity<ApiResponseWrapper<List<PassengerRideResponse>>> historyResponse =
                rideController.getPassengerRideHistory("p-10", "PASSENGER", "p-10");

        assertEquals(HttpStatus.OK, activeResponse.getStatusCode());
        assertEquals(1, activeResponse.getBody().getData().size());
        assertEquals("active-1", activeResponse.getBody().getData().getFirst().rideId());

        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertEquals(1, historyResponse.getBody().getData().size());
        assertEquals("history-1", historyResponse.getBody().getData().getFirst().rideId());

        verify(activeRidesService).findPassengerActiveRides("p-10");
        verify(activeRidesService).findPassengerRideHistory("p-10");
    }

    @Test
    void bookRide_whenPassengerInPayloadMismatchesHeader_shouldThrowUnauthorized() {
        JoinTripRequest request = JoinTripRequest.builder()
                .tripId("trip-1")
                .driverId("d-1")
                .requestedSeats(1)
                .build();

        assertThrows(UnauthorizedException.class, () -> rideController.bookRide("p-1", "PASSENGER", request));
    }

    @Test
    void cancelRide_whenUserInPayloadMismatchesHeader_shouldThrowUnauthorized() {
        CancelTripRequest request = CancelTripRequest.builder().rideId("b-1").build();

        assertThrows(UnauthorizedException.class, () -> rideController.cancelRide("p-1", "PASSENGER", request));
    }

    @Test
    void passengerRideEndpoints_whenPathPassengerDoesNotMatchHeader_shouldThrowUnauthorized() {
        assertThrows(
                UnauthorizedException.class,
                () -> rideController.getPassengerActiveRides("other-user", "PASSENGER", "p-10"));
        assertThrows(
                UnauthorizedException.class,
                () -> rideController.getPassengerRideHistory("other-user", "PASSENGER", "p-10"));
    }

    @Test
    void bookRide_whenRoleIsNotPassenger_shouldThrowForbidden() {
        JoinTripRequest request = JoinTripRequest.builder()
                .tripId("trip-1")
                .driverId("d-1")
                .requestedSeats(1)
                .build();

        assertThrows(ForbiddenException.class, () -> rideController.bookRide("p-1", "DRIVER", request));
    }
}
