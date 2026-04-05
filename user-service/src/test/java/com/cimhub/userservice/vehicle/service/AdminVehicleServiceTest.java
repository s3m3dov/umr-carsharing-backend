package com.cimhub.userservice.vehicle.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.response.AdminVehicleResponse;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.model.Vehicle;
import com.cimhub.userservice.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        RegisterVehicleRequest request = new RegisterVehicleRequest(
                "driver@example.com",
                "Tesla Model 3",
                "he-xx-1234",
                "sedan",
                "white",
                "5"
        );

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

        verify(auditService).logCreate(EntityType.VEHICLE, "vehicle-1", "admin@example.com", "Vehicle created by admin");
    }

    @Test
    void getVehicle_whenNotFound_shouldThrowResourceNotFound() {
        when(vehicleRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminVehicleService.getVehicle("missing-id"));
    }
}
