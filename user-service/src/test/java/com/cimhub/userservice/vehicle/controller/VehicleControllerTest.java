package com.cimhub.userservice.vehicle.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.dto.response.VehicleResponse;
import com.cimhub.userservice.vehicle.service.VehicleService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    @Test
    void healthCheck_shouldReturnSuccessMessage() {
        var response = vehicleController.healthCheck();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Vehicle Service is alive", response.getBody().getMessage());
    }

    @Test
    void getUserVehicles_shouldReturnVehicleList() {
        when(vehicleService.findUserVehicles("driver@example.com"))
                .thenReturn(List.of(new VehicleResponse("HE-XX-1234", "Tesla | white", "5")));

        var response = vehicleController.getUserVehicles("driver@example.com", UserType.DRIVER, "driver@example.com");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void registerVehicle_shouldReturnCreatedStatus() {
        RegisterVehicleRequest request =
                new RegisterVehicleRequest("driver@example.com", "Tesla", "HE-XX-1234", "sedan", "white", "5");
        when(vehicleService.registerVehicle(request))
                .thenReturn(new VehicleResponse("HE-XX-1234", "Tesla | white", "5"));

        var response = vehicleController.registerVehicle("driver@example.com", UserType.DRIVER, request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("HE-XX-1234", response.getBody().getData().value());
        verify(vehicleService).registerVehicle(request);
    }

    @Test
    void getUserVehicles_withMismatchedOwner_shouldThrowForbidden() {
        assertThrows(
                ForbiddenException.class,
                () -> vehicleController.getUserVehicles("other@example.com", UserType.DRIVER, "driver@example.com"));
    }
}
