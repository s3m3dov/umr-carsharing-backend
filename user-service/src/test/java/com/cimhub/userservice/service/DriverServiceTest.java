package com.cimhub.userservice.service;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.userservice.dto.request.CreateDriverProfileRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    private DriverService driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverService(driverRepository);
    }

    @Test
    void createDriverProfile_happyPath() {
        String userId = "driver@example.com";
        CreateDriverProfileRequest request = new CreateDriverProfileRequest(
                "John", "Doe", "+1234567890", 25, "DL123456");

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        DriverResponse response = driverService.createDriverProfile(userId, request);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertEquals(25, response.getAge());
        assertEquals("DL123456", response.getLicenseNumber());
        assertEquals(UserType.DRIVER, response.getUserType());
        assertEquals(DriverStatus.PENDING, response.getDriverStatus());

        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void createDriverProfile_whenAlreadyExists_throwsUserAlreadyExists() {
        String userId = "driver@example.com";
        CreateDriverProfileRequest request = new CreateDriverProfileRequest(
                "John", "Doe", "+1234567890", 25, "DL123456");

        Driver existingDriver = new Driver();
        existingDriver.setUserId(userId);
        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(existingDriver));

        assertThrows(UserAlreadyExistsException.class,
                () -> driverService.createDriverProfile(userId, request));
    }

    @Test
    void getDriverProfile_whenFound_returnsResponse() {
        String userId = "driver@example.com";
        Driver driver = new Driver();
        driver.setUserId(userId);
        driver.setEmail(userId);
        driver.setFirstName("John");
        driver.setLastName("Doe");
        driver.setPhoneNumber("+1234567890");
        driver.setAge(25);
        driver.setLicenseNumber("DL123456");
        driver.setUserType(UserType.DRIVER);
        driver.setDriverStatus(DriverStatus.ACTIVE);
        driver.setCreatedAt(Instant.now());
        driver.setUpdatedAt(Instant.now());

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(driver));

        DriverResponse response = driverService.getDriverProfile(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertEquals(25, response.getAge());
        assertEquals("DL123456", response.getLicenseNumber());
        assertEquals(UserType.DRIVER, response.getUserType());
    }

    @Test
    void getDriverProfile_whenNotFound_throwsResourceNotFound() {
        String userId = "nonexistent@example.com";

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> driverService.getDriverProfile(userId));
    }

    @Test
    void updateDriverProfile_appliesOnlyNonNullFields() {
        String userId = "driver@example.com";
        Driver existingDriver = new Driver();
        existingDriver.setUserId(userId);
        existingDriver.setEmail(userId);
        existingDriver.setFirstName("John");
        existingDriver.setLastName("Doe");
        existingDriver.setPhoneNumber("+1234567890");
        existingDriver.setAge(25);
        existingDriver.setLicenseNumber("DL123456");
        existingDriver.setCreatedAt(Instant.now());
        existingDriver.setUpdatedAt(Instant.now());

        UpdateDriverRequest updateRequest = new UpdateDriverRequest(
                null, "Smith", "+0987654321", null, null);

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(existingDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        DriverResponse response = driverService.updateDriverProfile(userId, updateRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("+0987654321", response.getPhoneNumber());
        assertEquals(25, response.getAge());
        assertEquals("DL123456", response.getLicenseNumber());

        verify(driverRepository).save(any(Driver.class));
    }
}