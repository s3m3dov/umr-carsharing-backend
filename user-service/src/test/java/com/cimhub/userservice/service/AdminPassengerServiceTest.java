package com.cimhub.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.client.AuthServiceClient;
import com.cimhub.userservice.client.NotificationServiceClient;
import com.cimhub.userservice.client.ReviewServiceClient;
import com.cimhub.userservice.client.TripServiceClient;
import com.cimhub.userservice.dto.request.CreatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.repository.PassengerRepository;
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
class AdminPassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

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

    private AdminPassengerService adminPassengerService;

    @BeforeEach
    void setUp() {
        adminPassengerService = new AdminPassengerService(
                passengerRepository,
                auditService,
                authServiceClient,
                tripServiceClient,
                reviewServiceClient,
                notificationServiceClient);
    }

    @Test
    void createPassenger_shouldThrowBadRequest() {
        CreatePassengerRequest request =
                new CreatePassengerRequest("passenger@example.com", "Pat", "Rider", "+49 100 200300", 27);

        assertThrows(
                BadRequestException.class, () -> adminPassengerService.createPassenger(request, "admin@example.com"));

        verify(passengerRepository, never()).save(any(Passenger.class));
        verify(auditService, never()).logCreate(any(), any(), any(), any());
    }

    @Test
    void getPassenger_whenMissing_shouldThrowResourceNotFound() {
        when(passengerRepository.findByUserId("missing-passenger")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminPassengerService.getPassenger("missing-passenger"));
    }

    @Test
    void listPassengers_shouldMapPagedProfiles() {
        Passenger p1 = new Passenger();
        p1.setUserId("p1@example.com");
        p1.setEmail("p1@example.com");
        p1.setUserType(UserType.PASSENGER);
        p1.setFirstName("P1");
        p1.setLastName("One");
        p1.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        p1.setUpdatedAt(Instant.parse("2026-01-01T11:00:00Z"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Passenger> page = new PageImpl<>(List.of(p1), pageable, 1);

        when(passengerRepository.findAll(pageable)).thenReturn(page);

        Page<PassengerResponse> responses = adminPassengerService.listPassengers(pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals("p1@example.com", responses.getContent().getFirst().userId());
    }

    @Test
    void updatePassenger_shouldApplyProvidedFieldsAndAudit() {
        Passenger passenger = new Passenger();
        passenger.setUserId("p3@example.com");
        passenger.setEmail("p3@example.com");
        passenger.setUserType(UserType.PASSENGER);
        passenger.setFirstName("Old");
        passenger.setLastName("Name");
        passenger.setPhoneNumber("111");
        passenger.setAge(20);

        when(passengerRepository.findByUserId("p3@example.com")).thenReturn(Optional.of(passenger));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassengerResponse response = adminPassengerService.updatePassenger(
                "p3@example.com",
                new com.cimhub.userservice.dto.request.UpdatePassengerRequest("New", "Passenger", "999", 31),
                "admin@example.com");

        assertEquals("New", response.firstName());
        assertEquals("Passenger", response.lastName());
        assertEquals("999", response.phoneNumber());
        assertEquals(31, response.age());
        assertEquals("p3@example.com", response.email());
        verify(auditService)
                .logUpdate(EntityType.PASSENGER, "p3@example.com", "admin@example.com", "Passenger info updated");
    }

    @Test
    void deletePassenger_shouldDeleteRecordsAndAudit() {
        Passenger passenger = new Passenger();
        passenger.setUserId("p4@example.com");
        passenger.setEmail("p4@example.com");
        passenger.setUserType(UserType.PASSENGER);

        when(passengerRepository.findByUserId("p4@example.com")).thenReturn(Optional.of(passenger));

        adminPassengerService.deletePassenger("p4@example.com", "admin@example.com");

        verify(passengerRepository).delete(passenger);
        verify(authServiceClient).deleteUser("p4@example.com");
        verify(tripServiceClient).deleteUserData("p4@example.com");
        verify(reviewServiceClient).deleteUserData("p4@example.com");
        verify(notificationServiceClient).deleteUserData("p4@example.com");
        verify(auditService)
                .logDelete(EntityType.PASSENGER, "p4@example.com", "admin@example.com", "Passenger deleted");
    }
}
