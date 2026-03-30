package com.cimhub.userservice.dto.response;

import lombok.Builder;

@Builder
public record AdminVehicleResponse(
        String id,
        String userId,
        String vehicleNumber,
        String vehicleName,
        String vehicleType,
        String vehicleColor,
        String seatingCapacity) {
}
