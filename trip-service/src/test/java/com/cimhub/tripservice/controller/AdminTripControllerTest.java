package com.cimhub.tripservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.service.trip.AdminTripService;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class AdminTripControllerTest {

    @Mock
    private AdminTripService adminTripService;

    private AdminTripController adminTripController;

    @BeforeEach
    void setUp() {
        adminTripController = new AdminTripController(adminTripService);
    }

    @Test
    void listTrips_withAdminRole_shouldReturnPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        AdminTripResponse trip = AdminTripResponse.builder().tripId("trip-1").build();
        Page<AdminTripResponse> page = new PageImpl<>(List.of(trip), pageable, 1);
        when(adminTripService.listTrips(null, pageable)).thenReturn(page);

        ResponseEntity<ApiSuccess<Page<AdminTripResponse>>> response =
                adminTripController.listTrips("ADMIN", null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(adminTripService).listTrips(null, pageable);
    }

    @Test
    void getTrip_withAdminRole_shouldReturnTrip() {
        AdminTripResponse trip = AdminTripResponse.builder().tripId("trip-1").build();
        when(adminTripService.getTrip("trip-1")).thenReturn(trip);

        ResponseEntity<ApiSuccess<AdminTripResponse>> response = adminTripController.getTrip("ADMIN", "trip-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("trip-1", response.getBody().getData().tripId());
        verify(adminTripService).getTrip("trip-1");
    }

    @Test
    void upcomingAndHistory_withAdminRole_shouldReturnPages() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<AdminTripResponse> page = new PageImpl<>(List.of(), pageable, 0);
        when(adminTripService.getUpcomingTrips(pageable)).thenReturn(page);
        when(adminTripService.getTripHistory(pageable)).thenReturn(page);

        ResponseEntity<ApiSuccess<Page<AdminTripResponse>>> upcoming =
                adminTripController.getUpcomingTrips("ADMIN", pageable);
        ResponseEntity<ApiSuccess<Page<AdminTripResponse>>> history =
                adminTripController.getTripHistory("admin", pageable);

        assertEquals(HttpStatus.OK, upcoming.getStatusCode());
        assertEquals(HttpStatus.OK, history.getStatusCode());
        verify(adminTripService).getUpcomingTrips(pageable);
        verify(adminTripService).getTripHistory(pageable);
    }

    @Test
    void cancelTrip_withAdminRole_shouldDelegateAndReturnMessage() {
        ResponseEntity<ApiSuccess<String>> response =
                adminTripController.cancelTrip("ADMIN", "admin@example.com", "trip-9");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Trip cancelled", response.getBody().getMessage());
        verify(adminTripService).cancelTrip("trip-9", "admin@example.com");
    }

    @Test
    void endpoints_withNonAdminRole_shouldThrowForbidden() {
        Pageable pageable = PageRequest.of(0, 1);

        assertThrows(ForbiddenException.class, () -> adminTripController.listTrips("PASSENGER", null, pageable));
        assertThrows(ForbiddenException.class, () -> adminTripController.getTrip("DRIVER", "trip-1"));
        assertThrows(ForbiddenException.class, () -> adminTripController.getUpcomingTrips("USER", pageable));
        assertThrows(ForbiddenException.class, () -> adminTripController.getTripHistory("GUEST", pageable));
        assertThrows(
                ForbiddenException.class,
                () -> adminTripController.cancelTrip("passenger", "admin@example.com", "trip-1"));
    }
}
