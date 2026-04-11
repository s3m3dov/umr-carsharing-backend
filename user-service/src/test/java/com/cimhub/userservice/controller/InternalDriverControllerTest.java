package com.cimhub.userservice.controller;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import com.cimhub.userservice.dto.request.CreateDriverProfileRequest;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.service.DriverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalDriverControllerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private InternalDriverController internalDriverController;

    @Test
    void createDriverProfile_shouldReturnCreatedStatus() {
        CreateDriverProfileRequest request = new CreateDriverProfileRequest("John", "Doe", "+123", 30, "LIC-1");
        DriverResponse driverResponse = DriverResponse.builder()
                .userId("driver@example.com")
                .userType(UserType.DRIVER)
                .driverStatus(DriverStatus.PENDING)
                .build();
        when(driverService.createDriverProfile("driver@example.com", request)).thenReturn(driverResponse);

        var response = internalDriverController.createDriverProfile("driver@example.com", request);

        assertEquals(201, response.getStatusCode().value());
        verify(driverService).createDriverProfile("driver@example.com", request);
    }
}
