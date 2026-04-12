package com.cimhub.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.client.AuthServiceClient;
import com.cimhub.userservice.client.NotificationServiceClient;
import com.cimhub.userservice.client.ReviewServiceClient;
import com.cimhub.userservice.client.TripServiceClient;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.DriverDocument;
import com.cimhub.userservice.repository.DriverDocumentRepository;
import com.cimhub.userservice.repository.DriverRepository;
import com.cimhub.userservice.vehicle.repository.VehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminDriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverDocumentRepository driverDocumentRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private TripServiceClient tripServiceClient;

    @Mock
    private ReviewServiceClient reviewServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    private AdminDriverService adminDriverService;

    @BeforeEach
    void setUp() {
        adminDriverService = new AdminDriverService(
                driverRepository,
                driverDocumentRepository,
                vehicleRepository,
                auditService,
                authServiceClient,
                tripServiceClient,
                reviewServiceClient,
                notificationServiceClient);
    }

    @Test
    void approveDriver_shouldSetActiveAndAudit() {
        Driver driver = new Driver();
        driver.setUserId("driver@example.com");
        driver.setEmail("driver@example.com");
        driver.setUserType(UserType.DRIVER);
        driver.setDriverStatus(DriverStatus.PENDING);

        when(driverRepository.findByUserId("driver@example.com")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = adminDriverService.approveDriver("driver@example.com", "admin@example.com");

        assertEquals(DriverStatus.ACTIVE, response.driverStatus());
        verify(auditService)
                .logApprove(EntityType.DRIVER, "driver@example.com", "admin@example.com", "Driver approved");
    }

    @Test
    void listDrivers_withStatus_shouldReturnMappedPage() {
        Driver driver = new Driver();
        driver.setUserId("driver-1");
        driver.setEmail("driver1@example.com");
        driver.setUserType(UserType.DRIVER);
        driver.setDriverStatus(DriverStatus.ACTIVE);
        driver.setLicenseNumber("LIC-1");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Driver> driversPage = new PageImpl<>(List.of(driver), pageable, 1);

        when(driverRepository.findByDriverStatus(DriverStatus.ACTIVE, pageable)).thenReturn(driversPage);

        Page<DriverResponse> responses = adminDriverService.listDrivers(DriverStatus.ACTIVE, pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals("driver-1", responses.getContent().getFirst().userId());
        assertEquals(DriverStatus.ACTIVE, responses.getContent().getFirst().driverStatus());
    }

    @Test
    void listDrivers_withoutStatus_shouldUseDriverRepository() {
        Driver driver = new Driver();
        driver.setUserId("driver-2");
        driver.setEmail("driver2@example.com");
        driver.setUserType(UserType.DRIVER);
        driver.setDriverStatus(DriverStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Driver> page = new PageImpl<>(List.of(driver), pageable, 1);
        when(driverRepository.findAll(pageable)).thenReturn(page);

        Page<DriverResponse> responses = adminDriverService.listDrivers(null, pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals("driver-2", responses.getContent().getFirst().userId());
    }

    @Test
    void updateDriver_shouldApplyProvidedFieldsAndAudit() {
        Driver driver = new Driver();
        driver.setUserId("driver-4");
        driver.setEmail("driver-4@example.com");
        driver.setUserType(UserType.DRIVER);
        driver.setFirstName("Old");
        driver.setLastName("Name");
        driver.setPhoneNumber("111");
        driver.setAge(20);
        driver.setLicenseNumber("OLD-LIC");

        when(driverRepository.findByUserId("driver-4")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = adminDriverService.updateDriver(
                "driver-4",
                new com.cimhub.userservice.dto.request.UpdateDriverRequest("New", "Driver", "999", 31, "NEW-LIC"),
                "admin@example.com");

        assertEquals("New", response.firstName());
        assertEquals("Driver", response.lastName());
        assertEquals("999", response.phoneNumber());
        assertEquals(31, response.age());
        assertEquals("NEW-LIC", response.licenseNumber());
        assertEquals("driver-4@example.com", response.email());
        verify(auditService).logUpdate(EntityType.DRIVER, "driver-4", "admin@example.com", "Driver info updated");
    }

    @Test
    void deleteDriver_shouldRemoveDriverAndAudit() {
        Driver driver = new Driver();
        driver.setUserId("driver-5");
        driver.setUserType(UserType.DRIVER);

        when(driverRepository.findByUserId("driver-5")).thenReturn(Optional.of(driver));

        adminDriverService.deleteDriver("driver-5", "admin@example.com");

        verify(driverRepository).deleteByUserId("driver-5");
        verify(driverDocumentRepository).deleteByDriverId("driver-5");
        verify(vehicleRepository).deleteByUserId("driver-5");
        verify(authServiceClient).deleteUser("driver-5");
        verify(tripServiceClient).deleteUserData("driver-5");
        verify(reviewServiceClient).deleteUserData("driver-5");
        verify(notificationServiceClient).deleteUserData("driver-5");
        verify(auditService).logDelete(EntityType.DRIVER, "driver-5", "admin@example.com", "Driver deleted");
    }

    @Test
    void getDriverDocuments_whenDriverMissing_shouldThrowResourceNotFound() {
        when(driverRepository.findByUserId("missing-driver")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminDriverService.getDriverDocuments("missing-driver"));

        verify(driverDocumentRepository, never()).findByDriverId(any());
    }

    @Test
    void getDriverDocuments_shouldMapDocumentList() {
        Driver driver = new Driver();
        driver.setUserId("driver-6");
        driver.setUserType(UserType.DRIVER);

        DriverDocument doc = DriverDocument.builder()
                .id("doc-1")
                .driverId("driver-6")
                .documentType("LICENSE")
                .fileName("license.pdf")
                .fileUrl("https://example.test/license.pdf")
                .fileSize(1000L)
                .uploadedAt(Instant.parse("2026-01-05T10:00:00Z"))
                .build();

        when(driverRepository.findByUserId("driver-6")).thenReturn(Optional.of(driver));
        when(driverDocumentRepository.findByDriverId("driver-6")).thenReturn(List.of(doc));

        var responses = adminDriverService.getDriverDocuments("driver-6");

        assertEquals(1, responses.size());
        assertEquals("doc-1", responses.getFirst().id());
    }
}
