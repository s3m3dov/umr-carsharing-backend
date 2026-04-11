package com.cimhub.userservice.controller;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.dto.request.BulkDriverActionRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverDocumentResponse;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.service.AdminDriverService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDriverControllerTest {

    @Mock
    private AdminDriverService adminDriverService;

    @InjectMocks
    private AdminDriverController adminDriverController;

    @Test
    void listDrivers_withAdminRole_shouldReturnPage() {
        Page<DriverResponse> page = new PageImpl<>(List.of(DriverResponse.builder().userId("driver-1").build()));
        when(adminDriverService.listDrivers(DriverStatus.ACTIVE, PageRequest.of(0, 10))).thenReturn(page);

        var response = adminDriverController.listDrivers(UserType.ADMIN, DriverStatus.ACTIVE, PageRequest.of(0, 10));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().getTotalElements());
    }

    @Test
    void listDrivers_withNonAdminRole_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> adminDriverController.listDrivers(UserType.PASSENGER, null, PageRequest.of(0, 10)));
    }

    @Test
    void approveDriver_shouldDelegateAndReturnResponse() {
        DriverResponse approved = DriverResponse.builder().userId("driver-1").driverStatus(DriverStatus.ACTIVE).build();
        when(adminDriverService.approveDriver("driver-1", "admin@example.com")).thenReturn(approved);

        var response = adminDriverController.approveDriver(UserType.ADMIN, "admin@example.com", "driver-1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(DriverStatus.ACTIVE, response.getBody().getData().driverStatus());
    }

    @Test
    void bulkApprove_shouldReturnSuccessMessage() {
        when(adminDriverService.bulkApprove(any(BulkDriverActionRequest.class), any())).thenReturn(2);

        var response = adminDriverController.bulkApprove(
                UserType.ADMIN,
                "admin@example.com",
                new BulkDriverActionRequest(List.of("d1", "d2")));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("2 drivers approved", response.getBody().getMessage());
    }

    @Test
    void updateDriver_shouldDelegateToService() {
        UpdateDriverRequest request = new UpdateDriverRequest("New", null, null, null, null);
        DriverResponse updated = DriverResponse.builder().userId("driver-1").firstName("New").userType(UserType.DRIVER).build();
        when(adminDriverService.updateDriver("driver-1", request, "admin@example.com")).thenReturn(updated);

        var response = adminDriverController.updateDriver(UserType.ADMIN, "admin@example.com", "driver-1", request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("New", response.getBody().getData().firstName());
    }

    @Test
    void deleteDriver_shouldCallServiceAndReturnMessage() {
        var response = adminDriverController.deleteDriver(UserType.ADMIN, "admin@example.com", "driver-1");

        verify(adminDriverService).deleteDriver("driver-1", "admin@example.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Driver deleted", response.getBody().getMessage());
    }

    @Test
    void getDriverDocuments_shouldReturnDocumentList() {
        when(adminDriverService.getDriverDocuments("driver-1"))
                .thenReturn(List.of(DriverDocumentResponse.builder().id("doc-1").driverId("driver-1").build()));

        var response = adminDriverController.getDriverDocuments(UserType.ADMIN, "driver-1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().size());
    }
}
