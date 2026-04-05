package com.cimhub.userservice.vehicle.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.vehicle.dto.request.RegisterVehicleRequest;
import com.cimhub.userservice.vehicle.dto.response.VehicleResponse;
import com.cimhub.userservice.vehicle.model.Vehicle;
import com.cimhub.userservice.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuditService auditService;

    public List<VehicleResponse> findUserVehicles(String userId) {
        log.info("Fetching vehicles for userId: {}", userId);
        return vehicleRepository.findByUserId(userId).stream()
                .map(this::toVehicleResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponse registerVehicle(RegisterVehicleRequest request) {
        log.info("Registering new vehicle: {} for userId: {}", request.vehicleNumber(), request.userId());

        String normalizedVehicleNumber = request.vehicleNumber().toUpperCase(Locale.ROOT);

        if (vehicleRepository.existsByVehicleNumber(normalizedVehicleNumber)) {
            log.warn("Vehicle with number {} already exists", request.vehicleNumber());
            return null;
        }

        Vehicle newVehicle = new Vehicle();
        newVehicle.setUserId(request.userId());
        newVehicle.setVehicleName(request.vehicleName());
        newVehicle.setVehicleNumber(normalizedVehicleNumber);
        newVehicle.setVehicleType(request.vehicleType());
        newVehicle.setVehicleColor(request.vehicleColor());
        newVehicle.setSeatingCapacity(request.seatingCapacity());
        Instant now = Instant.now();
        newVehicle.setCreatedAt(now);
        newVehicle.setUpdatedAt(now);

        Vehicle savedVehicle = vehicleRepository.save(newVehicle);
        auditService.logCreate(EntityType.VEHICLE, savedVehicle.getId(), request.userId(), "Vehicle registered by user");
        log.info("Vehicle registered successfully: {}", request.vehicleNumber());
        return toVehicleResponse(savedVehicle);
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getVehicleNumber(),
                vehicle.getVehicleName() + " | " + vehicle.getVehicleColor(),
                vehicle.getSeatingCapacity());
    }
}
