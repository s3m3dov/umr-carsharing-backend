package com.cimhub.userservice.controller;

import com.cimhub.common.enums.UserType;
import com.cimhub.userservice.dto.request.CreatePassengerProfileRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.service.PassengerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalPassengerControllerTest {

    @Mock
    private PassengerService passengerService;

    @InjectMocks
    private InternalPassengerController internalPassengerController;

    @Test
    void createPassengerProfile_shouldReturnCreatedStatus() {
        CreatePassengerProfileRequest request = new CreatePassengerProfileRequest("Jane", "Doe", "+123", 28);
        PassengerResponse passengerResponse = PassengerResponse.builder()
                .userId("passenger@example.com")
                .userType(UserType.PASSENGER)
                .build();
        when(passengerService.createPassengerProfile("passenger@example.com", request)).thenReturn(passengerResponse);

        var response = internalPassengerController.createPassengerProfile("passenger@example.com", request);

        assertEquals(201, response.getStatusCode().value());
        verify(passengerService).createPassengerProfile("passenger@example.com", request);
    }
}
