package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.tripservice.dto.request.UpdateBookingRequest;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.service.booking.AdminBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBookingControllerTest {

    @Mock
    private AdminBookingService adminBookingService;

    private AdminBookingController adminBookingController;

    @BeforeEach
    void setUp() {
        adminBookingController = new AdminBookingController(adminBookingService);
    }

    @Test
    void listAndGet_withAdminRole_shouldReturnResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        BookingResponse booking = BookingResponse.builder().bookingId("b-1").build();
        Page<BookingResponse> page = new PageImpl<>(List.of(booking), pageable, 1);
        when(adminBookingService.listBookings(pageable)).thenReturn(page);
        when(adminBookingService.getBooking("b-1")).thenReturn(booking);

        ResponseEntity<ApiResponseWrapper<Page<BookingResponse>>> listResponse =
                adminBookingController.listBookings("ADMIN", pageable);
        ResponseEntity<ApiResponseWrapper<BookingResponse>> getResponse =
                adminBookingController.getBooking("ADMIN", "b-1");

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(1, listResponse.getBody().getData().getTotalElements());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("b-1", getResponse.getBody().getData().bookingId());
    }

    @Test
    void updateAndCancel_withAdminRole_shouldDelegate() {
        UpdateBookingRequest request = new UpdateBookingRequest(BookingStatus.CONFIRMED, 2);
        BookingResponse updated = BookingResponse.builder().bookingId("b-2").status(BookingStatus.CONFIRMED).build();
        when(adminBookingService.updateBooking("b-2", request, "admin@example.com")).thenReturn(updated);

        ResponseEntity<ApiResponseWrapper<BookingResponse>> updateResponse =
                adminBookingController.updateBooking("ADMIN", "admin@example.com", "b-2", request);
        ResponseEntity<ApiResponseWrapper<String>> cancelResponse =
                adminBookingController.cancelBooking("admin", "admin@example.com", "b-3");

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("b-2", updateResponse.getBody().getData().bookingId());
        assertEquals(HttpStatus.OK, cancelResponse.getStatusCode());
        assertEquals("Booking cancelled", cancelResponse.getBody().getMessage());

        verify(adminBookingService).updateBooking("b-2", request, "admin@example.com");
        verify(adminBookingService).cancelBooking("b-3", "admin@example.com");
    }

    @Test
    void getPassengerBookings_withAdminRole_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<BookingResponse> page = new PageImpl<>(List.of(), pageable, 0);
        when(adminBookingService.getPassengerBookings("p-1", pageable)).thenReturn(page);

        ResponseEntity<ApiResponseWrapper<Page<BookingResponse>>> response =
                adminBookingController.getPassengerBookings("ADMIN", "p-1", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminBookingService).getPassengerBookings("p-1", pageable);
    }

    @Test
    void endpoints_withNonAdminRole_shouldThrowForbidden() {
        Pageable pageable = PageRequest.of(0, 1);
        UpdateBookingRequest request = new UpdateBookingRequest(BookingStatus.CANCELLED, 1);

        assertThrows(ForbiddenException.class,
                () -> adminBookingController.listBookings("PASSENGER", pageable));
        assertThrows(ForbiddenException.class,
                () -> adminBookingController.getBooking("DRIVER", "b-1"));
        assertThrows(ForbiddenException.class,
                () -> adminBookingController.updateBooking("USER", "admin@example.com", "b-2", request));
        assertThrows(ForbiddenException.class,
                () -> adminBookingController.cancelBooking("guest", "admin@example.com", "b-3"));
        assertThrows(ForbiddenException.class,
                () -> adminBookingController.getPassengerBookings("DRIVER", "p-1", pageable));
    }
}
