package com.cimhub.userservice.vehicle.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.dto.request.UpdateVehicleRequest;
import com.cimhub.userservice.dto.response.AdminVehicleResponse;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.service.AdminVehicleService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminVehicleControllerTest {

    @Mock
    private AdminVehicleService adminVehicleService;

    @InjectMocks
    private AdminVehicleController adminVehicleController;

    @Test
    void listVehicles_withAdminRole_shouldReturnPage() {
        Page<AdminVehicleResponse> page =
                new PageImpl<>(List.of(AdminVehicleResponse.builder().id("v1").build()));
        when(adminVehicleService.listVehicles(PageRequest.of(0, 10))).thenReturn(page);

        var response = adminVehicleController.listVehicles(UserType.ADMIN, PageRequest.of(0, 10));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().getTotalElements());
    }

    @Test
    void listVehicles_withNonAdminRole_shouldThrowForbidden() {
        assertThrows(
                ForbiddenException.class,
                () -> adminVehicleController.listVehicles(UserType.DRIVER, PageRequest.of(0, 10)));
    }

    @Test
    void getVehicle_shouldReturnResponse() {
        when(adminVehicleService.getVehicle("v1"))
                .thenReturn(AdminVehicleResponse.builder().id("v1").build());

        var response = adminVehicleController.getVehicle(UserType.ADMIN, "v1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("v1", response.getBody().getData().id());
    }

    @Test
    void createVehicle_shouldReturnCreatedStatus() {
        RegisterVehicleRequest request =
                new RegisterVehicleRequest("driver@example.com", "Tesla", "HE-XX-1234", "sedan", "white", "5");
        when(adminVehicleService.createVehicle(request, "admin@example.com"))
                .thenReturn(AdminVehicleResponse.builder()
                        .id("v1")
                        .vehicleNumber("HE-XX-1234")
                        .build());

        var response = adminVehicleController.createVehicle(UserType.ADMIN, "admin@example.com", request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("v1", response.getBody().getData().id());
    }

    @Test
    void updateVehicle_shouldReturnUpdatedResponse() {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Tesla 3", null, null, null);
        when(adminVehicleService.updateVehicle("v1", request, "admin@example.com"))
                .thenReturn(AdminVehicleResponse.builder()
                        .id("v1")
                        .vehicleName("Tesla 3")
                        .build());

        var response = adminVehicleController.updateVehicle(UserType.ADMIN, "admin@example.com", "v1", request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Tesla 3", response.getBody().getData().vehicleName());
    }

    @Test
    void deleteVehicle_shouldCallServiceAndReturnMessage() {
        var response = adminVehicleController.deleteVehicle(UserType.ADMIN, "admin@example.com", "v1");

        verify(adminVehicleService).deleteVehicle("v1", "admin@example.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Vehicle deleted", response.getBody().getMessage());
    }
}
