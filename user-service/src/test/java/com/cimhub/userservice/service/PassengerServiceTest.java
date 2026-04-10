package com.cimhub.userservice.service;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.userservice.dto.request.CreatePassengerProfileRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.model.Passenger;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    private PassengerService passengerService;

    @BeforeEach
    void setUp() {
        passengerService = new PassengerService(passengerRepository);
    }

    @Test
    void createPassengerProfile_happyPath() {
        String userId = "passenger@example.com";
        CreatePassengerProfileRequest request = new CreatePassengerProfileRequest(
                "Jane", "Doe", "+1234567890", 30);

        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));

        PassengerResponse response = passengerService.createPassengerProfile(userId, request);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertEquals(30, response.getAge());
        assertEquals(UserType.PASSENGER, response.getUserType());

        verify(passengerRepository).save(any(Passenger.class));
    }

    @Test
    void createPassengerProfile_whenAlreadyExists_throwsUserAlreadyExists() {
        String userId = "passenger@example.com";
        CreatePassengerProfileRequest request = new CreatePassengerProfileRequest(
                "Jane", "Doe", "+1234567890", 30);

        Passenger existingPassenger = new Passenger();
        existingPassenger.setUserId(userId);
        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.of(existingPassenger));

        assertThrows(UserAlreadyExistsException.class,
                () -> passengerService.createPassengerProfile(userId, request));
    }

    @Test
    void getPassengerProfile_whenFound_returnsResponse() {
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

        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.of(passenger));

        PassengerResponse response = passengerService.getPassengerProfile(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertEquals(30, response.getAge());
        assertEquals(UserType.PASSENGER, response.getUserType());
    }

    @Test
    void getPassengerProfile_whenNotFound_throwsResourceNotFound() {
        String userId = "nonexistent@example.com";

        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> passengerService.getPassengerProfile(userId));
    }

    @Test
    void updatePassengerProfile_appliesOnlyNonNullFields() {
        String userId = "passenger@example.com";
        Passenger existingPassenger = new Passenger();
        existingPassenger.setUserId(userId);
        existingPassenger.setEmail(userId);
        existingPassenger.setFirstName("Jane");
        existingPassenger.setLastName("Doe");
        existingPassenger.setPhoneNumber("+1234567890");
        existingPassenger.setAge(30);
        existingPassenger.setCreatedAt(Instant.now());
        existingPassenger.setUpdatedAt(Instant.now());

        UpdatePassengerRequest updateRequest = new UpdatePassengerRequest(
                null, "Smith", "+0987654321", null);

        when(passengerRepository.findByUserId(userId)).thenReturn(Optional.of(existingPassenger));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));

        PassengerResponse response = passengerService.updatePassengerProfile(userId, updateRequest);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("+0987654321", response.getPhoneNumber());
        assertEquals(30, response.getAge());

        verify(passengerRepository).save(any(Passenger.class));
    }
}