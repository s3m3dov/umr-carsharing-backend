package com.cimhub.userservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.request.CreatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.model.User;
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
class AdminPassengerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private AuditService auditService;

    private AdminPassengerService adminPassengerService;

    @BeforeEach
    void setUp() {
        adminPassengerService = new AdminPassengerService(userRepository, passengerRepository, auditService);
    }

    @Test
    void createPassenger_shouldPersistPassengerWithEnumTypeAndAudit() {
        CreatePassengerRequest request = new CreatePassengerRequest(
                "passenger@example.com",
                "Pat",
                "Rider",
                "+49 100 200300",
                27
        );

        when(userRepository.findByEmail(request.email())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passengerRepository.findByUserId(request.email())).thenReturn(Optional.empty());

        PassengerResponse response = adminPassengerService.createPassenger(request, "admin@example.com");

        assertEquals(request.email(), response.userId());
        assertEquals(request.email(), response.email());
        assertEquals(UserType.PASSENGER, response.userType());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserType.PASSENGER, userCaptor.getValue().getUserType());

        verify(auditService).logCreate(EntityType.PASSENGER, request.email(), "admin@example.com", "Passenger created by admin");
    }

    @Test
    void createPassenger_whenEmailExists_shouldThrowUserAlreadyExists() {
        CreatePassengerRequest request = new CreatePassengerRequest(
                "exists@example.com",
                "Existing",
                "Passenger",
                "+49 111 222333",
                30
        );
        User existing = new User();
        existing.setEmail(request.email());

        when(userRepository.findByEmail(request.email())).thenReturn(existing);

        assertThrows(UserAlreadyExistsException.class,
                () -> adminPassengerService.createPassenger(request, "admin@example.com"));

        verify(userRepository, never()).save(any(User.class));
        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    void getPassenger_whenMissing_shouldThrowResourceNotFound() {
        when(userRepository.findByUserId("missing-passenger")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> adminPassengerService.getPassenger("missing-passenger"));
    }
}
