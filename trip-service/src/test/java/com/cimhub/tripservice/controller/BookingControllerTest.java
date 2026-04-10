package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.dto.response.CompleteRideResponse;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.OperationMessage;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.service.booking.BookingService;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.booking.CompletionService;
import com.cimhub.tripservice.service.booking.JoinTripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private JoinTripService joinTripService;

    @Mock
    private CancellationService cancellationService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CompletionService completionService;

    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        bookingController = new BookingController(joinTripService, bookingService, cancellationService, completionService);
    }

    @Test
    void getBooking_shouldReturnOkWithServiceResult() {
        BookingResponse bookingResponse = BookingResponse.builder()
                .bookingId("b-0")
                .status(BookingStatus.COMPLETED)
                .build();
        when(bookingService.getBooking("b-0")).thenReturn(bookingResponse);

        ResponseEntity<ApiResponseWrapper<BookingResponse>> response = bookingController.getBooking("b-0");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("b-0", response.getBody().getData().bookingId());
        assertEquals(BookingStatus.COMPLETED, response.getBody().getData().status());
        verify(bookingService).getBooking("b-0");
    }

    @Test
    void acceptBooking_shouldReturnOkWithServiceResult() {
        when(joinTripService.acceptBooking("b-1", "driver-1")).thenReturn("Booking accepted");

        ResponseEntity<ApiResponseWrapper<String>> response = bookingController.acceptBooking("b-1", "driver-1", "DRIVER");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Booking accepted", response.getBody().getMessage());
        verify(joinTripService).acceptBooking("b-1", "driver-1");
    }

    @Test
    void rejectBooking_shouldReturnOkWithServiceResult() {
        when(joinTripService.rejectBooking("b-2", "driver-1")).thenReturn("Booking rejected");

        ResponseEntity<ApiResponseWrapper<String>> response = bookingController.rejectBooking("b-2", "driver-1", "DRIVER");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Booking rejected", response.getBody().getMessage());
        verify(joinTripService).rejectBooking("b-2", "driver-1");
    }

    @Test
    void completeRide_whenDriverParamMismatchesHeader_shouldThrowUnauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> bookingController.completeRide("b-3", "driver-1", "DRIVER", "other-driver"));
    }

    @Test
    void acceptBooking_whenRoleIsNotDriver_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> bookingController.acceptBooking("b-1", "driver-1", "PASSENGER"));
    }

    @Test
    void completeRide_shouldReturnOkWithServiceResult() {
        CompleteRideResponse completeRideResponse = CompleteRideResponse.builder()
                .bookingId("b-3")
                .tripId("trip-3")
                .driverId("driver-1")
                .passengerId("passenger-1")
                .bookingStatus(BookingStatus.COMPLETED)
                .tripStatus(TripStatus.IN_PROGRESS)
                .build();
        when(completionService.completeRide("b-3", "driver-1")).thenReturn(completeRideResponse);

        ResponseEntity<ApiResponseWrapper<CompleteRideResponse>> response = bookingController.completeRide("b-3", "driver-1", "DRIVER", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OperationMessage.RIDE_COMPLETED_SUCCESS.text(), response.getBody().getMessage());
        assertEquals(BookingStatus.COMPLETED, response.getBody().getData().bookingStatus());
        assertEquals(TripStatus.IN_PROGRESS, response.getBody().getData().tripStatus());
        verify(completionService).completeRide("b-3", "driver-1");
    }
}
