package com.cimhub.userservice.dto.request;

public record UpdateVehicleRequest(
        String vehicleName,
        String vehicleType,
        String vehicleColor,
        String seatingCapacity) {
}
