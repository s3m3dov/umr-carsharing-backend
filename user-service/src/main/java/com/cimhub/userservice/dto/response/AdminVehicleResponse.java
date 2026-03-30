package com.cimhub.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(
        name = "AdminVehicleResponse",
        description = "Detailed vehicle response for admin vehicle management endpoints",
        example = """
                {
                  "id": "vehicle_001",
                  "userId": "user_123",
                  "vehicleNumber": "HE-XX-1234",
                  "vehicleName": "Tesla Model 3",
                  "vehicleType": "sedan",
                  "vehicleColor": "white",
                  "seatingCapacity": "5",
                  "createdAt": "2026-03-30T12:00:00.000Z",
                  "updatedAt": "2026-03-30T12:05:45.456Z"
                }
                """
)
public record AdminVehicleResponse(
        String id,
        String userId,
        String vehicleNumber,
        String vehicleName,
        String vehicleType,
        String vehicleColor,
        String seatingCapacity,
        @Schema(description = "Timestamp when vehicle was created (UTC)", example = "2026-03-30T12:00:00.000Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,
        @Schema(description = "Timestamp when vehicle was last updated (UTC)", example = "2026-03-30T12:05:45.456Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt) {
}
