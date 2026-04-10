package com.cimhub.userservice.service;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.repository.DriverRepository;
import com.cimhub.userservice.repository.PassengerRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PassengerRepository passengerRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(driverRepository, passengerRepository);
    }

    @Test
    void getUserInfo_whenDriverExists_returnsDriverResponse() {
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
        driver.setCreatedAt(Instant.now());
        driver.setUpdatedAt(Instant.now());

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(driver));
        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UserResponse response = userService.getUserInfo(userId);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(UserType.DRIVER, response.userType());
        assertEquals("+1234567890", response.phoneNumber());
        assertEquals(25, response.age());
        assertEquals("DL123456", response.licenseNumber());
    }

    @Test
    void getUserInfo_whenPassengerExists_returnsPassengerResponse() {
        String userId = "passenger@example.com";
        Passenger passenger = new Passenger();
        passenger.setUserId(userId);
        passenger.setEmail(userId);
        passenger.setFirstName("Jane");
        passenger.setLastName("Doe");
        passenger.setPhoneNumber("+1234567890");
        passenger.setAge(30);
        passenger.setUserType(UserType.PASSENGER);
        passenger.setCreatedAt(Instant.now());
        passenger.setUpdatedAt(Instant.now());

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.of(passenger));

        UserResponse response = userService.getUserInfo(userId);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("Jane", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(UserType.PASSENGER, response.userType());
        assertEquals("+1234567890", response.phoneNumber());
        assertEquals(30, response.age());
        assertEquals(null, response.licenseNumber());
    }

    @Test
    void getUserInfo_whenNeitherExists_throwsResourceNotFound() {
        String userId = "nonexistent@example.com";

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserInfo(userId));
    }
}