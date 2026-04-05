package com.cimhub.userservice.vehicle.service;

import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.dto.response.VehicleResponse;
import com.cimhub.userservice.vehicle.model.Vehicle;
import com.cimhub.userservice.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditService auditService;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, auditService);
    }

    // ── registerVehicle ───────────────────────────────────────────────────────

    @Test
    void registerVehicle_withNewPlate_shouldUppercasePersistAndReturnTrue() {
        RegisterVehicleRequest request = new RegisterVehicleRequest(
                "driver@example.com",
                "Tesla Model 3",
                "he-xx-1234",
                "sedan",
                "white",
                "5");

        when(vehicleRepository.existsByVehicleNumber("HE-XX-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId("vehicle-1");
            return v;
        });

        boolean result = vehicleService.registerVehicle(request);

        assertTrue(result);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle saved = captor.getValue();
        assertEquals("HE-XX-1234", saved.getVehicleNumber()); // uppercased
        assertEquals("driver@example.com", saved.getUserId());
        assertEquals("Tesla Model 3", saved.getVehicleName());
        assertEquals("sedan", saved.getVehicleType());
        assertEquals("white", saved.getVehicleColor());
        assertEquals("5", saved.getSeatingCapacity());
    }

    @Test
    void registerVehicle_withDuplicatePlate_shouldReturnFalseWithoutPersisting() {
        RegisterVehicleRequest request = new RegisterVehicleRequest(
                "driver@example.com",
                "BMW X5",
                "HE-XX-9999",
                "suv",
                "black",
                "7");

        when(vehicleRepository.existsByVehicleNumber("HE-XX-9999")).thenReturn(true);

        boolean result = vehicleService.registerVehicle(request);

        assertFalse(result);
        verify(vehicleRepository, never()).save(any());
        verify(auditService, never()).logCreate(any(), any(), any(), any());
    }

    @Test
    void registerVehicle_plateNormalizationIsAppliedBeforeDuplicateCheck() {
        // Lowercase input "he-xx-1234" must be uppercased to "HE-XX-1234" before the uniqueness check
        RegisterVehicleRequest request = new RegisterVehicleRequest(
                "driver@example.com",
                "Mini Cooper",
                "he-xx-1234",
                "hatchback",
                "red",
                "4");

        when(vehicleRepository.existsByVehicleNumber("HE-XX-1234")).thenReturn(true);

        boolean result = vehicleService.registerVehicle(request);

        assertFalse(result);
        verify(vehicleRepository).existsByVehicleNumber("HE-XX-1234");
        verify(vehicleRepository, never()).save(any());
    }

    // ── findUserVehicles ──────────────────────────────────────────────────────

    @Test
    void findUserVehicles_shouldReturnMappedResponsesWithFormattedText() {
        Vehicle v1 = new Vehicle();
        v1.setVehicleNumber("HE-XX-1234");
        v1.setVehicleName("Tesla Model 3");
        v1.setVehicleColor("white");
        v1.setSeatingCapacity("5");

        Vehicle v2 = new Vehicle();
        v2.setVehicleNumber("MR-AB-5678");
        v2.setVehicleName("BMW X5");
        v2.setVehicleColor("black");
        v2.setSeatingCapacity("7");

        when(vehicleRepository.findByUserId("driver@example.com")).thenReturn(List.of(v1, v2));

        List<VehicleResponse> responses = vehicleService.findUserVehicles("driver@example.com");

        assertEquals(2, responses.size());
        // value = vehicleNumber, text = vehicleName + " | " + vehicleColor
        assertEquals("HE-XX-1234", responses.get(0).value());
        assertEquals("Tesla Model 3 | white", responses.get(0).text());
        assertEquals("5", responses.get(0).seatingCapacity());

        assertEquals("MR-AB-5678", responses.get(1).value());
        assertEquals("BMW X5 | black", responses.get(1).text());
    }

    @Test
    void findUserVehicles_whenNoneExist_shouldReturnEmptyList() {
        when(vehicleRepository.findByUserId("driver@example.com")).thenReturn(List.of());

        List<VehicleResponse> responses = vehicleService.findUserVehicles("driver@example.com");

        assertTrue(responses.isEmpty());
    }
}
