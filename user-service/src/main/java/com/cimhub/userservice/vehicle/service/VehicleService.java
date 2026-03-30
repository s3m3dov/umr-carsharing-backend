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

import java.util.List;
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
                .map(vehicle -> new VehicleResponse(
                        vehicle.getVehicleNumber(),
                        vehicle.getVehicleName() + " | " + vehicle.getVehicleColor(),
                        vehicle.getSeatingCapacity()))
                .collect(Collectors.toList());
    }

    public boolean registerVehicle(RegisterVehicleRequest request) {
        log.info("Registering new vehicle: {} for userId: {}", request.vehicleNumber(), request.userId());

        if (vehicleRepository.existsByVehicleNumber(request.vehicleNumber().toUpperCase())) {
            log.warn("Vehicle with number {} already exists", request.vehicleNumber());
            return false;
        }

        Vehicle newVehicle = new Vehicle();
        newVehicle.setUserId(request.userId());
        newVehicle.setVehicleName(request.vehicleName());
        newVehicle.setVehicleNumber(request.vehicleNumber().toUpperCase());
        newVehicle.setVehicleType(request.vehicleType());
        newVehicle.setVehicleColor(request.vehicleColor());
        newVehicle.setSeatingCapacity(request.seatingCapacity());

        Vehicle savedVehicle = vehicleRepository.save(newVehicle);
        auditService.logCreate(EntityType.VEHICLE, savedVehicle.getId(), request.userId(), "Vehicle registered by user");
        log.info("Vehicle registered successfully: {}", request.vehicleNumber());
        return true;
    }
}
