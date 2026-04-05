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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void listPassengers_shouldMapPagedUsers() {
        User u1 = new User();
        u1.setUserId("p1@example.com");
        u1.setEmail("p1@example.com");
        u1.setUserType(UserType.PASSENGER);
        u1.setFirstName("P1");
        u1.setLastName("One");
        u1.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        u1.setUpdatedAt(Instant.parse("2026-01-01T11:00:00Z"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(u1), pageable, 1);

        when(userRepository.findByUserType(UserType.PASSENGER, pageable)).thenReturn(page);
        when(passengerRepository.findByUserId("p1@example.com")).thenReturn(Optional.empty());

        Page<PassengerResponse> responses = adminPassengerService.listPassengers(pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals("p1@example.com", responses.getContent().getFirst().userId());
    }

    @Test
    void getPassenger_whenExists_shouldReturnMappedResponse() {
        User user = new User();
        user.setUserId("p2@example.com");
        user.setEmail("p2@example.com");
        user.setUserType(UserType.PASSENGER);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setCreatedAt(Instant.parse("2026-01-03T10:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-01-03T10:30:00Z"));

        Passenger passenger = new Passenger();
        passenger.setUserId("p2@example.com");
        passenger.setCreatedAt(Instant.parse("2026-01-02T10:00:00Z"));
        passenger.setUpdatedAt(Instant.parse("2026-01-04T10:00:00Z"));

        when(userRepository.findByUserId("p2@example.com")).thenReturn(user);
        when(passengerRepository.findByUserId("p2@example.com")).thenReturn(Optional.of(passenger));

        PassengerResponse response = adminPassengerService.getPassenger("p2@example.com");

        assertEquals("p2@example.com", response.userId());
        assertEquals("Jane", response.firstName());
        assertEquals(UserType.PASSENGER, response.userType());
        assertEquals(Instant.parse("2026-01-02T10:00:00Z"), response.createdAt());
        assertEquals(Instant.parse("2026-01-04T10:00:00Z"), response.updatedAt());
    }

    @Test
    void updatePassenger_shouldApplyProvidedFieldsAndAudit() {
        User user = new User();
        user.setUserId("p3@example.com");
        user.setEmail("p3@example.com");
        user.setUserType(UserType.PASSENGER);
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setPhoneNumber("111");
        user.setAge(20);

        Passenger passenger = new Passenger();
        passenger.setUserId("p3@example.com");

        when(userRepository.findByUserId("p3@example.com")).thenReturn(user);
        when(passengerRepository.findByUserId("p3@example.com")).thenReturn(Optional.of(passenger));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassengerResponse response = adminPassengerService.updatePassenger(
                "p3@example.com",
                new com.cimhub.userservice.dto.request.UpdatePassengerRequest("New", "Passenger", "999", 31),
                "admin@example.com");

        assertEquals("New", response.firstName());
        assertEquals("Passenger", response.lastName());
        assertEquals("999", response.phoneNumber());
        assertEquals(31, response.age());
        verify(auditService).logUpdate(EntityType.PASSENGER, "p3@example.com", "admin@example.com",
                "Passenger info updated");
    }

    @Test
    void deletePassenger_shouldDeleteRecordsAndAudit() {
        User user = new User();
        user.setUserId("p4@example.com");
        user.setEmail("p4@example.com");
        user.setUserType(UserType.PASSENGER);

        Passenger passenger = new Passenger();
        passenger.setUserId("p4@example.com");

        when(userRepository.findByUserId("p4@example.com")).thenReturn(user);
        when(passengerRepository.findByUserId("p4@example.com")).thenReturn(Optional.of(passenger));

        adminPassengerService.deletePassenger("p4@example.com", "admin@example.com");

        verify(passengerRepository).delete(passenger);
        verify(userRepository).delete(user);
        verify(auditService).logDelete(EntityType.PASSENGER, "p4@example.com", "admin@example.com",
                "Passenger deleted");
    }

    @Test
    void updatePassenger_whenPassengerProfileMissing_shouldStillUpdateUserAndAudit() {
        User user = new User();
        user.setUserId("p5@example.com");
        user.setEmail("p5@example.com");
        user.setUserType(UserType.PASSENGER);
        user.setFirstName("Base");

        when(userRepository.findByUserId("p5@example.com")).thenReturn(user);
        when(passengerRepository.findByUserId("p5@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassengerResponse response = adminPassengerService.updatePassenger(
                "p5@example.com",
                new com.cimhub.userservice.dto.request.UpdatePassengerRequest("Updated", null, null, null),
                "admin@example.com");

        assertEquals("Updated", response.firstName());
        assertNotNull(response.updatedAt());
        verify(auditService).logUpdate(EntityType.PASSENGER, "p5@example.com", "admin@example.com",
                "Passenger info updated");
        verify(passengerRepository, never()).save(any(Passenger.class));
    }
}
