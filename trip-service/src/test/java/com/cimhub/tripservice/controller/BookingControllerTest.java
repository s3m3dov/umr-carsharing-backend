package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.JoinTripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private JoinTripService joinTripService;

    @Mock
    private CancellationService cancellationService;

    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        bookingController = new BookingController(joinTripService, cancellationService);
    }

    @Test
    void acceptBooking_shouldReturnOkWithServiceResult() {
        when(joinTripService.acceptBooking("b-1")).thenReturn("Booking accepted");

        ResponseEntity<ApiResponseWrapper<String>> response = bookingController.acceptBooking("b-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Booking accepted", response.getBody().getMessage());
        verify(joinTripService).acceptBooking("b-1");
    }

    @Test
    void rejectBooking_shouldReturnOkWithServiceResult() {
        when(joinTripService.rejectBooking("b-2")).thenReturn("Booking rejected");

        ResponseEntity<ApiResponseWrapper<String>> response = bookingController.rejectBooking("b-2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Booking rejected", response.getBody().getMessage());
        verify(joinTripService).rejectBooking("b-2");
    }
}
