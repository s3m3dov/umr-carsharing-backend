package com.cimhub.userservice.vehicle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.response.AdminVehicleResponse;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.model.Vehicle;
import com.cimhub.userservice.vehicle.repository.VehicleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminVehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditService auditService;

    private AdminVehicleService adminVehicleService;

    @BeforeEach
    void setUp() {
        adminVehicleService = new AdminVehicleService(vehicleRepository, auditService);
    }

    @Test
    void createVehicle_shouldUppercaseVehicleNumberAndAudit() {
        RegisterVehicleRequest request =
                new RegisterVehicleRequest("driver@example.com", "Tesla Model 3", "he-xx-1234", "sedan", "white", "5");

        when(vehicleRepository.existsByUserId("driver@example.com")).thenReturn(false);
        when(vehicleRepository.existsByVehicleNumber("HE-XX-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle saved = invocation.getArgument(0);
            saved.setId("vehicle-1");
            return saved;
        });

        AdminVehicleResponse response = adminVehicleService.createVehicle(request, "admin@example.com");

        assertEquals("vehicle-1", response.id());
        assertEquals("HE-XX-1234", response.vehicleNumber());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals("HE-XX-1234", vehicleCaptor.getValue().getVehicleNumber());

        verify(auditService)
                .logCreate(EntityType.VEHICLE, "vehicle-1", "admin@example.com", "Vehicle created by admin");
    }

    @Test
    void createVehicle_whenDriverAlreadyHasVehicle_shouldThrowBadRequest() {
        RegisterVehicleRequest request =
                new RegisterVehicleRequest("driver@example.com", "Tesla Model 3", "he-xx-1234", "sedan", "white", "5");

        when(vehicleRepository.existsByUserId("driver@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> adminVehicleService.createVehicle(request, "admin@example.com"));

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(auditService, never()).logCreate(any(), any(), any(), any());
    }

    @Test
    void createVehicle_whenVehicleNumberExists_shouldThrowBadRequest() {
        RegisterVehicleRequest request =
                new RegisterVehicleRequest("driver@example.com", "Tesla Model 3", "he-xx-1234", "sedan", "white", "5");

        when(vehicleRepository.existsByUserId("driver@example.com")).thenReturn(false);
        when(vehicleRepository.existsByVehicleNumber("HE-XX-1234")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> adminVehicleService.createVehicle(request, "admin@example.com"));

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(auditService, never()).logCreate(any(), any(), any(), any());
    }

    @Test
    void getVehicle_whenNotFound_shouldThrowResourceNotFound() {
        when(vehicleRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminVehicleService.getVehicle("missing-id"));
    }
}
