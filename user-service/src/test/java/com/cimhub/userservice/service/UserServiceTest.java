package com.cimhub.userservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.repository.DriverRepository;
import com.cimhub.userservice.repository.PassengerRepository;
import com.cimhub.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private AuditService auditService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, driverRepository, passengerRepository, auditService);
    }

    @Test
    void createProfile_whenExistingUser_shouldThrowUserAlreadyExists() {
        CreateProfileRequest request = new CreateProfileRequest(
                "user-1",
                "user@example.com",
                UserType.PASSENGER,
                "John",
                "Doe",
                "+123",
                30,
                null);

        when(userRepository.findByUserId("user-1")).thenReturn(new com.cimhub.userservice.model.User());

        assertThrows(UserAlreadyExistsException.class, () -> userService.createProfile(request));
        verify(userRepository, never()).save(any(com.cimhub.userservice.model.User.class));
    }

    @Test
    void createProfile_forDriver_shouldCreateDriverProfileAndAudit() {
        CreateProfileRequest request = new CreateProfileRequest(
                "driver-1",
                "driver@example.com",
                UserType.DRIVER,
                "Jane",
                "Driver",
                "+998",
                28,
                "LIC-123");

        when(userRepository.findByUserId("driver-1")).thenReturn(null);
        when(driverRepository.findByUserId("driver-1")).thenReturn(Optional.of(new Driver()));

        UserResponse response = userService.createProfile(request);

        assertEquals("driver-1", response.userId());
        assertEquals(UserType.DRIVER, response.userType());

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertEquals("LIC-123", driverCaptor.getValue().getLicenseNumber());

        verify(auditService).logCreate(EntityType.DRIVER, "driver-1", "driver@example.com", "User profile created");
    }

    @Test
    void getUserInfo_whenMissingUser_shouldThrowNotFound() {
        when(userRepository.findByUserId("missing-user")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserInfo("missing-user"));
    }
}
