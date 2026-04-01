package com.cimhub.userservice.service;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.model.Driver;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
}
