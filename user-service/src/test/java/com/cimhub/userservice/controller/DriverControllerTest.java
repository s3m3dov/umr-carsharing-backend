package com.cimhub.userservice.controller;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.service.DriverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    @Test
    void getDriverProfile_shouldReturnWrappedResponse() {
        DriverResponse driverResponse = DriverResponse.builder()
                .userId("driver@example.com")
                .firstName("John")
                .lastName("Doe")
                .email("driver@example.com")
                .userType(UserType.DRIVER)
                .phoneNumber("+123")
                .age(30)
                .licenseNumber("LIC-1")
                .driverStatus(DriverStatus.ACTIVE)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T10:05:00Z"))
                .build();
        when(driverService.getDriverProfile("driver@example.com")).thenReturn(driverResponse);

        var response = driverController.getDriverProfile("driver@example.com", UserType.DRIVER, "driver@example.com");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("driver@example.com", response.getBody().getData().userId());
    }

    @Test
    void updateDriverProfile_shouldReturnUpdatedResponse() {
        UpdateDriverRequest request = new UpdateDriverRequest("Jane", null, null, null, null);
        DriverResponse driverResponse = DriverResponse.builder()
                .userId("driver@example.com")
                .firstName("Jane")
                .userType(UserType.DRIVER)
                .build();
        when(driverService.updateDriverProfile("driver@example.com", request)).thenReturn(driverResponse);

        var response = driverController.updateDriverProfile(
                "driver@example.com",
                UserType.DRIVER,
                "driver@example.com",
                request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Jane", response.getBody().getData().firstName());
    }

    @Test
    void getDriverProfile_withMismatchedOwner_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> driverController.getDriverProfile("other@example.com", UserType.DRIVER, "driver@example.com"));
    }
}
