package com.cimhub.userservice.service;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.DriverDocument;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.DriverDocumentRepository;
import com.cimhub.userservice.repository.DriverRepository;
import com.cimhub.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDriverServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverDocumentRepository driverDocumentRepository;

    @Mock
    private AuditService auditService;

    private AdminDriverService adminDriverService;

    @BeforeEach
    void setUp() {
        adminDriverService = new AdminDriverService(
                userRepository,
                driverRepository,
                driverDocumentRepository,
                auditService
        );
    }

    @Test
    void approveDriver_shouldSetActiveAndAudit() {
        User user = new User();
        user.setUserId("driver@example.com");
        user.setEmail("driver@example.com");
        user.setUserType(UserType.DRIVER);

        Driver driver = new Driver();
        driver.setUserId(user.getUserId());
        driver.setDriverStatus(DriverStatus.PENDING);

        when(userRepository.findByUserId(user.getUserId())).thenReturn(user);
        when(driverRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(driver));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = adminDriverService.approveDriver(user.getUserId(), "admin@example.com");

        assertEquals(UserType.DRIVER, response.userType());
        assertEquals(DriverStatus.ACTIVE, response.driverStatus());

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertEquals(DriverStatus.ACTIVE, driverCaptor.getValue().getDriverStatus());

        verify(auditService).logApprove(EntityType.DRIVER, user.getUserId(), "admin@example.com", "Driver approved");
    }

    @Test
    void getDriver_whenUserIsNotDriver_shouldThrowResourceNotFound() {
        User passenger = new User();
        passenger.setUserId("passenger@example.com");
        passenger.setUserType(UserType.PASSENGER);

        when(userRepository.findByUserId(passenger.getUserId())).thenReturn(passenger);

        assertThrows(ResourceNotFoundException.class,
                () -> adminDriverService.getDriver(passenger.getUserId()));
    }

    @Test
    void listDrivers_withStatus_shouldReturnMappedPage() {
        Driver driver = new Driver();
        driver.setUserId("driver-1");
        driver.setDriverStatus(DriverStatus.ACTIVE);
        driver.setLicenseNumber("LIC-1");

        User user = new User();
        user.setUserId("driver-1");
        user.setUserType(UserType.DRIVER);
        user.setEmail("driver1@example.com");
        user.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-01-02T10:00:00Z"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Driver> driversPage = new PageImpl<>(List.of(driver), pageable, 1);

        when(driverRepository.findByDriverStatus(DriverStatus.ACTIVE, pageable)).thenReturn(driversPage);
        when(userRepository.findByUserIdIn(List.of("driver-1"))).thenReturn(List.of(user));

        Page<DriverResponse> responses = adminDriverService.listDrivers(DriverStatus.ACTIVE, pageable);

        assertEquals(1, responses.getTotalElements());
        DriverResponse response = responses.getContent().getFirst();
        assertEquals("driver-1", response.userId());
        assertEquals(DriverStatus.ACTIVE, response.driverStatus());
        assertEquals("LIC-1", response.licenseNumber());
    }

    @Test
    void listDrivers_withoutStatus_shouldUseUserRepositoryAndMapDefaults() {
        User user = new User();
        user.setUserId("driver-2");
        user.setUserType(UserType.DRIVER);
        user.setEmail("driver2@example.com");
        user.setCreatedAt(Instant.parse("2026-01-03T10:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-01-04T10:00:00Z"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> usersPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findByUserType(UserType.DRIVER, pageable)).thenReturn(usersPage);
        when(driverRepository.findByUserIdIn(List.of("driver-2"))).thenReturn(List.of());

        Page<DriverResponse> responses = adminDriverService.listDrivers(null, pageable);

        assertEquals(1, responses.getTotalElements());
        DriverResponse response = responses.getContent().getFirst();
        assertEquals("driver-2", response.userId());
        assertEquals(DriverStatus.PENDING, response.driverStatus());
        assertNull(response.licenseNumber());
    }

    @Test
    void rejectDriver_shouldSetRejectedAndAudit() {
        User user = new User();
        user.setUserId("driver-3");
        user.setUserType(UserType.DRIVER);

        Driver driver = new Driver();
        driver.setUserId("driver-3");
        driver.setDriverStatus(DriverStatus.PENDING);

        when(userRepository.findByUserId("driver-3")).thenReturn(user);
        when(driverRepository.findByUserId("driver-3")).thenReturn(Optional.of(driver));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = adminDriverService.rejectDriver("driver-3", "admin@example.com");

        assertEquals(DriverStatus.REJECTED, response.driverStatus());
        verify(auditService).logReject(EntityType.DRIVER, "driver-3", "admin@example.com", "Driver rejected");
    }

    @Test
    void bulkApprove_shouldCountOnlyExistingDrivers() {
        User existing = new User();
        existing.setUserId("driver-1");
        existing.setUserType(UserType.DRIVER);

        Driver existingProfile = new Driver();
        existingProfile.setUserId("driver-1");

        when(userRepository.findByUserId("driver-1")).thenReturn(existing);
        when(userRepository.findByUserId("missing")).thenReturn(null);
        when(driverRepository.findByUserId("driver-1")).thenReturn(Optional.of(existingProfile));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int count = adminDriverService.bulkApprove(
                new com.cimhub.userservice.dto.request.BulkDriverActionRequest(List.of("driver-1", "missing")),
                "admin@example.com");

        assertEquals(1, count);
    }

    @Test
    void bulkReject_shouldCountOnlyExistingDrivers() {
        User existing = new User();
        existing.setUserId("driver-7");
        existing.setUserType(UserType.DRIVER);

        Driver existingProfile = new Driver();
        existingProfile.setUserId("driver-7");

        when(userRepository.findByUserId("driver-7")).thenReturn(existing);
        when(userRepository.findByUserId("ghost")).thenReturn(null);
        when(driverRepository.findByUserId("driver-7")).thenReturn(Optional.of(existingProfile));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int count = adminDriverService.bulkReject(
                new com.cimhub.userservice.dto.request.BulkDriverActionRequest(List.of("driver-7", "ghost")),
                "admin@example.com");

        assertEquals(1, count);
    }

    @Test
    void updateDriver_shouldApplyAllProvidedFieldsAndAudit() {
        User user = new User();
        user.setUserId("driver-4");
        user.setUserType(UserType.DRIVER);
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setPhoneNumber("111");
        user.setAge(20);

        Driver driver = new Driver();
        driver.setUserId("driver-4");
        driver.setLicenseNumber("OLD-LIC");

        when(userRepository.findByUserId("driver-4")).thenReturn(user);
        when(driverRepository.findByUserId("driver-4")).thenReturn(Optional.of(driver));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
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
        verify(auditService).logUpdate(EntityType.DRIVER, "driver-4", "admin@example.com", "Driver info updated");
    }

    @Test
    void deleteDriver_shouldRemoveEntitiesAndAudit() {
        User user = new User();
        user.setUserId("driver-5");
        user.setUserType(UserType.DRIVER);

        when(userRepository.findByUserId("driver-5")).thenReturn(user);

        adminDriverService.deleteDriver("driver-5", "admin@example.com");

        verify(driverRepository).deleteByUserId("driver-5");
        verify(userRepository).delete(user);
        verify(auditService).logDelete(EntityType.DRIVER, "driver-5", "admin@example.com", "Driver deleted");
    }

    @Test
    void getDriverDocuments_shouldMapDocumentList() {
        User user = new User();
        user.setUserId("driver-6");
        user.setUserType(UserType.DRIVER);

        DriverDocument doc = DriverDocument.builder()
                .id("doc-1")
                .driverId("driver-6")
                .documentType("LICENSE")
                .fileName("license.pdf")
                .fileUrl("https://example.test/license.pdf")
                .fileSize(1000L)
                .uploadedAt(Instant.parse("2026-01-05T10:00:00Z"))
                .build();

        when(userRepository.findByUserId("driver-6")).thenReturn(user);
        when(driverDocumentRepository.findByDriverId("driver-6")).thenReturn(List.of(doc));

        var responses = adminDriverService.getDriverDocuments("driver-6");

        assertEquals(1, responses.size());
        assertEquals("doc-1", responses.getFirst().id());
        assertEquals("LICENSE", responses.getFirst().documentType());
    }

    @Test
    void getDriverDocuments_whenDriverMissing_shouldThrowResourceNotFound() {
        when(userRepository.findByUserId("missing-driver")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> adminDriverService.getDriverDocuments("missing-driver"));

        verify(driverDocumentRepository, never()).findByDriverId(any());
    }

    @Test
    void approveDriver_whenProfileMissing_shouldCreateProfile() {
        User user = new User();
        user.setUserId("driver-new");
        user.setUserType(UserType.DRIVER);
        user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        user.setCreatedBy("driver-new@example.com");

        when(userRepository.findByUserId("driver-new")).thenReturn(user);
        when(driverRepository.findByUserId("driver-new")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = adminDriverService.approveDriver("driver-new", "admin@example.com");

        assertEquals(DriverStatus.ACTIVE, response.driverStatus());
        assertTrue(response.createdAt() != null);
    }
}
