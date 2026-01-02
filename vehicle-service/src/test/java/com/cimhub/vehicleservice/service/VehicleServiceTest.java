package com.cimhub.vehicleservice.service;

import com.cimhub.vehicleservice.dto.request.RegisterVehicleRequest;
import com.cimhub.vehicleservice.dto.response.VehicleResponse;
import com.cimhub.vehicleservice.model.Vehicle;
import com.cimhub.vehicleservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private RegisterVehicleRequest registerRequest;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterVehicleRequest(
                "user123",
                "Toyota Camry",
                "ABC123",
                "Sedan",
                "Blue",
                4);

        testVehicle = new Vehicle();
        testVehicle.setUserId("user123");
        testVehicle.setVehicleName("Toyota Camry");
        testVehicle.setVehicleNumber("ABC123");
        testVehicle.setVehicleType("Sedan");
        testVehicle.setVehicleColor("Blue");
        testVehicle.setSeatingCapacity(4);
    }

    @Test
    void registerVehicle_whenValidRequest_thenSuccessfullyRegistersVehicle() {
        // Given
        when(vehicleRepository.existsByVehicleNumber(anyString())).thenReturn(false);

        // When
        boolean result = vehicleService.registerVehicle(registerRequest);

        // Then
        assertThat(result).isTrue();

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());

        Vehicle savedVehicle = vehicleCaptor.getValue();
        assertThat(savedVehicle.getUserId()).isEqualTo("user123");
        assertThat(savedVehicle.getVehicleName()).isEqualTo("Toyota Camry");
        assertThat(savedVehicle.getVehicleNumber()).isEqualTo("ABC123");
        assertThat(savedVehicle.getVehicleType()).isEqualTo("Sedan");
        assertThat(savedVehicle.getVehicleColor()).isEqualTo("Blue");
        assertThat(savedVehicle.getSeatingCapacity()).isEqualTo(4);
    }

    @Test
    void registerVehicle_whenVehicleNumberAlreadyExists_thenReturnsFalse() {
        // Given
        when(vehicleRepository.existsByVehicleNumber(anyString())).thenReturn(true);

        // When
        boolean result = vehicleService.registerVehicle(registerRequest);

        // Then
        assertThat(result).isFalse();
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void registerVehicle_whenCalled_thenConvertsVehicleNumberToUpperCase() {
        // Given
        RegisterVehicleRequest lowercaseRequest = new RegisterVehicleRequest(
                "user123",
                "Honda Civic",
                "xyz789",
                "Sedan",
                "Red",
                4);
        when(vehicleRepository.existsByVehicleNumber(anyString())).thenReturn(false);

        // When
        vehicleService.registerVehicle(lowercaseRequest);

        // Then
        verify(vehicleRepository).existsByVehicleNumber("XYZ789");

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().getVehicleNumber()).isEqualTo("XYZ789");
    }

    @Test
    void findUserVehicles_whenUserHasVehicles_thenReturnsVehicleList() {
        // Given
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setVehicleNumber("ABC123");
        vehicle1.setVehicleName("Toyota Camry");
        vehicle1.setVehicleColor("Blue");
        vehicle1.setSeatingCapacity(4);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVehicleNumber("XYZ789");
        vehicle2.setVehicleName("Honda Civic");
        vehicle2.setVehicleColor("Red");
        vehicle2.setSeatingCapacity(5);

        when(vehicleRepository.findByUserId(anyString())).thenReturn(Arrays.asList(vehicle1, vehicle2));

        // When
        List<VehicleResponse> responses = vehicleService.findUserVehicles("user123");

        // Then
        assertThat(responses).hasSize(2);

        assertThat(responses.get(0).vehicleNumber()).isEqualTo("ABC123");
        assertThat(responses.get(0).vehicleNameAndColor()).isEqualTo("Toyota Camry | Blue");
        assertThat(responses.get(0).seatingCapacity()).isEqualTo(4);

        assertThat(responses.get(1).vehicleNumber()).isEqualTo("XYZ789");
        assertThat(responses.get(1).vehicleNameAndColor()).isEqualTo("Honda Civic | Red");
        assertThat(responses.get(1).seatingCapacity()).isEqualTo(5);

        verify(vehicleRepository).findByUserId("user123");
    }

    @Test
    void findUserVehicles_whenUserHasNoVehicles_thenReturnsEmptyList() {
        // Given
        when(vehicleRepository.findByUserId(anyString())).thenReturn(Collections.emptyList());

        // When
        List<VehicleResponse> responses = vehicleService.findUserVehicles("user123");

        // Then
        assertThat(responses).isEmpty();
        verify(vehicleRepository).findByUserId("user123");
    }

    @Test
    void findUserVehicles_whenCalled_thenFormatsVehicleNameAndColorCorrectly() {
        // Given
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("TEST123");
        vehicle.setVehicleName("Test Vehicle");
        vehicle.setVehicleColor("Green");
        vehicle.setSeatingCapacity(3);

        when(vehicleRepository.findByUserId(anyString())).thenReturn(Collections.singletonList(vehicle));

        // When
        List<VehicleResponse> responses = vehicleService.findUserVehicles("user123");

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).vehicleNameAndColor()).contains(" | ");
        assertThat(responses.get(0).vehicleNameAndColor()).isEqualTo("Test Vehicle | Green");
    }
}
