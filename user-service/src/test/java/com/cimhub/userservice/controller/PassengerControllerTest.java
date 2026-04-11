package com.cimhub.userservice.controller;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.service.PassengerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassengerControllerTest {

    @Mock
    private PassengerService passengerService;

    @InjectMocks
    private PassengerController passengerController;

    @Test
    void getPassengerProfile_shouldReturnWrappedResponse() {
        PassengerResponse passengerResponse = PassengerResponse.builder()
                .userId("passenger@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .email("passenger@example.com")
                .userType(UserType.PASSENGER)
                .phoneNumber("+123")
                .age(28)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T10:05:00Z"))
                .build();
        when(passengerService.getPassengerProfile("passenger@example.com")).thenReturn(passengerResponse);

        var response = passengerController.getPassengerProfile(
                "passenger@example.com",
                UserType.PASSENGER,
                "passenger@example.com");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("passenger@example.com", response.getBody().getData().userId());
    }

    @Test
    void updatePassengerProfile_shouldReturnUpdatedResponse() {
        UpdatePassengerRequest request = new UpdatePassengerRequest("Janet", null, null, null);
        PassengerResponse passengerResponse = PassengerResponse.builder()
                .userId("passenger@example.com")
                .firstName("Janet")
                .userType(UserType.PASSENGER)
                .build();
        when(passengerService.updatePassengerProfile("passenger@example.com", request)).thenReturn(passengerResponse);

        var response = passengerController.updatePassengerProfile(
                "passenger@example.com",
                UserType.PASSENGER,
                "passenger@example.com",
                request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Janet", response.getBody().getData().firstName());
    }

    @Test
    void getPassengerProfile_withMismatchedOwner_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> passengerController.getPassengerProfile("other@example.com", UserType.PASSENGER, "passenger@example.com"));
    }
}
