package com.cimhub.userservice.vehicle.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.request.UpdateVehicleRequest;
import com.cimhub.userservice.dto.response.AdminVehicleResponse;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.model.Vehicle;
import com.cimhub.userservice.vehicle.repository.VehicleRepository;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminVehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuditService auditService;

    public Page<AdminVehicleResponse> listVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable).map(this::mapToResponse);
    }

    public AdminVehicleResponse getVehicle(String vehicleId) {
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        return mapToResponse(vehicle);
    }

    public AdminVehicleResponse createVehicle(RegisterVehicleRequest request, String adminEmail) {
        if (vehicleRepository.existsByUserId(request.userId())) {
            throw new BadRequestException("Driver already has a vehicle registered");
        }

        String normalizedVehicleNumber = request.vehicleNumber().toUpperCase(Locale.ROOT);
        if (vehicleRepository.existsByVehicleNumber(normalizedVehicleNumber)) {
            throw new BadRequestException("Vehicle already registered");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(request.userId());
        vehicle.setVehicleName(request.vehicleName());
        vehicle.setVehicleNumber(normalizedVehicleNumber);
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setVehicleColor(request.vehicleColor());
        vehicle.setSeatingCapacity(request.seatingCapacity());
        Instant now = Instant.now();
        vehicle.setCreatedAt(now);
        vehicle.setUpdatedAt(now);

        Vehicle saved = vehicleRepository.save(vehicle);
        auditService.logCreate(EntityType.VEHICLE, saved.getId(), adminEmail, "Vehicle created by admin");
        log.info("Vehicle {} created by admin {}", saved.getId(), adminEmail);
        return mapToResponse(saved);
    }

    public AdminVehicleResponse updateVehicle(String vehicleId, UpdateVehicleRequest request, String adminEmail) {
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        if (request.vehicleName() != null) vehicle.setVehicleName(request.vehicleName());
        if (request.vehicleType() != null) vehicle.setVehicleType(request.vehicleType());
        if (request.vehicleColor() != null) vehicle.setVehicleColor(request.vehicleColor());
        if (request.seatingCapacity() != null) vehicle.setSeatingCapacity(request.seatingCapacity());
        vehicle.setUpdatedAt(Instant.now());

        vehicleRepository.save(vehicle);
        auditService.logUpdate(EntityType.VEHICLE, vehicleId, adminEmail, "Vehicle updated");
        log.info("Vehicle {} updated by {}", vehicleId, adminEmail);
        return mapToResponse(vehicle);
    }

    public void deleteVehicle(String vehicleId, String adminEmail) {
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        vehicleRepository.delete(vehicle);
        auditService.logDelete(EntityType.VEHICLE, vehicleId, adminEmail, "Vehicle deleted");
        log.info("Vehicle {} deleted by {}", vehicleId, adminEmail);
    }

    private AdminVehicleResponse mapToResponse(Vehicle v) {
        return AdminVehicleResponse.builder()
                .id(v.getId())
                .userId(v.getUserId())
                .vehicleNumber(v.getVehicleNumber())
                .vehicleName(v.getVehicleName())
                .vehicleType(v.getVehicleType())
                .vehicleColor(v.getVehicleColor())
                .seatingCapacity(v.getSeatingCapacity())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
