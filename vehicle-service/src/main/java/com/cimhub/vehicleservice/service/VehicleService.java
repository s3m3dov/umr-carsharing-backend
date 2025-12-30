package com.cimhub.vehicleservice.service;

import com.cimhub.vehicleservice.dto.VehicleRegisterRequestDTO;
import com.cimhub.vehicleservice.dto.VehicleResponseDTO;

import java.util.List;

public interface VehicleService {
    List<VehicleResponseDTO> getUserVehicles(String userId);
    boolean addNewVehicle(String userId, VehicleRegisterRequestDTO registerRequestDTO);
}