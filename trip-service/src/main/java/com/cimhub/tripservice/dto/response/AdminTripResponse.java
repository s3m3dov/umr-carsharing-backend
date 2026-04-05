package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(
        name = "AdminTripResponse",
        description = "Detailed trip response for admin trip management endpoints",
        example = """
                {
                  "tripId": "trip_001",
                  "driverId": "john.driver@example.com",
                  "vehicleNumber": "HE-XX-1234",
                  "carType": "SEDAN",
                  "tripStatus": "SCHEDULED",
                  "sourceAddress": {
                    "latitude": 50.8154,
                    "longitude": 8.6747
                  },
                  "destinationAddress": {
                    "latitude": 50.7265,
                    "longitude": 7.0895
                  },
                  "totalSeats": 4,
                  "bookedSeats": 1,
                  "availableSeats": 3,
                  "pricePerSeat": 14.50,
                  "routeDistance": 82.7,
                  "routeDuration": 73.2,
                  "tripStartDateTimeUTC": "2026-04-01T07:00:00Z",
                  "tripTimezone": "Europe/Berlin",
                  "createdAt": "2026-03-30T12:00:00.000Z",
                  "updatedAt": "2026-03-30T12:10:30.123Z"
                }
                """
)
public record AdminTripResponse(
        String tripId,
        String driverId,
        String vehicleNumber,
        String carType,
        TripStatus tripStatus,
        Points sourceAddress,
        Points destinationAddress,
        int totalSeats,
        int bookedSeats,
        int availableSeats,
        double pricePerSeat,
        double routeDistance,
        double routeDuration,
        Instant tripStartDateTimeUTC,
        String tripTimezone,
        @Schema(description = "Timestamp when trip was created (UTC)", example = "2026-03-30T12:00:00.000Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,
        @Schema(description = "Timestamp when trip was last updated (UTC)", example = "2026-03-30T12:10:30.123Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt) {
}
