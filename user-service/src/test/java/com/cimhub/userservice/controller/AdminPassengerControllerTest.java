package com.cimhub.userservice.controller;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.dto.request.CreatePassengerRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.service.AdminPassengerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPassengerControllerTest {

    @Mock
    private AdminPassengerService adminPassengerService;

    @InjectMocks
    private AdminPassengerController adminPassengerController;

    @Test
    void listPassengers_withAdminRole_shouldReturnPage() {
        Page<PassengerResponse> page = new PageImpl<>(List.of(PassengerResponse.builder().userId("p1").build()));
        when(adminPassengerService.listPassengers(PageRequest.of(0, 10))).thenReturn(page);

        var response = adminPassengerController.listPassengers(UserType.ADMIN, PageRequest.of(0, 10));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().getTotalElements());
    }

    @Test
    void listPassengers_withNonAdminRole_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> adminPassengerController.listPassengers(UserType.DRIVER, PageRequest.of(0, 10)));
    }

    @Test
    void getPassenger_shouldDelegateToService() {
        PassengerResponse payload = PassengerResponse.builder().userId("p1").userType(UserType.PASSENGER).build();
        when(adminPassengerService.getPassenger("p1")).thenReturn(payload);

        var response = adminPassengerController.getPassenger(UserType.ADMIN, "p1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("p1", response.getBody().getData().userId());
    }

    @Test
    void createPassenger_shouldReturnCreatedStatus() {
        CreatePassengerRequest request = new CreatePassengerRequest("p1@example.com", "Pat", "One", "+123", 20);
        PassengerResponse payload = PassengerResponse.builder().userId("p1").build();
        when(adminPassengerService.createPassenger(request, "admin@example.com")).thenReturn(payload);

        var response = adminPassengerController.createPassenger(UserType.ADMIN, "admin@example.com", request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("p1", response.getBody().getData().userId());
    }

    @Test
    void updatePassenger_shouldDelegateToService() {
        UpdatePassengerRequest request = new UpdatePassengerRequest("Pat", null, null, null);
        PassengerResponse payload = PassengerResponse.builder().userId("p1").firstName("Pat").build();
        when(adminPassengerService.updatePassenger("p1", request, "admin@example.com")).thenReturn(payload);

        var response = adminPassengerController.updatePassenger(UserType.ADMIN, "admin@example.com", "p1", request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Pat", response.getBody().getData().firstName());
    }

    @Test
    void deletePassenger_shouldCallServiceAndReturnMessage() {
        var response = adminPassengerController.deletePassenger(UserType.ADMIN, "admin@example.com", "p1");

        verify(adminPassengerService).deletePassenger("p1", "admin@example.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Passenger deleted", response.getBody().getMessage());
    }
}
