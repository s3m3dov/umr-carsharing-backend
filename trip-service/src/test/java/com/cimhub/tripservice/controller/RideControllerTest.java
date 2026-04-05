package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.service.ActiveRidesService;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.JoinTripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        JoinTripRequest request = JoinTripRequest.builder().tripId("trip-1").passengerId("p-1").requestedSeats(1).build();
        when(joinTripService.joinTrip(request)).thenReturn("Ride booked");

        ResponseEntity<ApiResponseWrapper<String>> response = rideController.bookRide(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Ride booked", response.getBody().getMessage());
        verify(joinTripService).joinTrip(request);
    }

    @Test
    void bookRideWithDriverAccept_shouldReturnCreatedWithServiceMessage() {
        JoinTripRequest request = JoinTripRequest.builder().tripId("trip-2").passengerId("p-2").requestedSeats(2).build();
        when(joinTripService.joinTripWithDriverAccept(request)).thenReturn("Booking requested");

        ResponseEntity<ApiResponseWrapper<String>> response = rideController.bookRideWithDriverAccept(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Booking requested", response.getBody().getMessage());
        verify(joinTripService).joinTripWithDriverAccept(request);
    }

    @Test
    void cancelRide_shouldReturnOkWithServiceMessage() {
        CancelTripRequest request = CancelTripRequest.builder().userId("p-1").rideId("b-1").build();
        when(cancellationService.cancelRide(request)).thenReturn("Cancelled");

        ResponseEntity<ApiResponseWrapper<String>> response = rideController.cancelRide(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cancelled", response.getBody().getMessage());
        verify(cancellationService).cancelRide(request);
    }

    @Test
    void getPassengerActiveAndHistory_shouldReturnLists() {
        PassengerRideResponse active = PassengerRideResponse.builder().rideId("active-1").build();
        PassengerRideResponse history = PassengerRideResponse.builder().rideId("history-1").build();
        when(activeRidesService.findPassengerActiveRides("p-10")).thenReturn(List.of(active));
        when(activeRidesService.findPassengerRideHistory("p-10")).thenReturn(List.of(history));

        ResponseEntity<ApiResponseWrapper<List<PassengerRideResponse>>> activeResponse =
                rideController.getPassengerActiveRides("p-10");
        ResponseEntity<ApiResponseWrapper<List<PassengerRideResponse>>> historyResponse =
                rideController.getPassengerRideHistory("p-10");

        assertEquals(HttpStatus.OK, activeResponse.getStatusCode());
        assertEquals(1, activeResponse.getBody().getData().size());
        assertEquals("active-1", activeResponse.getBody().getData().getFirst().rideId());

        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertEquals(1, historyResponse.getBody().getData().size());
        assertEquals("history-1", historyResponse.getBody().getData().getFirst().rideId());

        verify(activeRidesService).findPassengerActiveRides("p-10");
        verify(activeRidesService).findPassengerRideHistory("p-10");
    }
}
