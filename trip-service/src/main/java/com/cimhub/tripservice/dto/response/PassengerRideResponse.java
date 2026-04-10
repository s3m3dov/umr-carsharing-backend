package com.cimhub.tripservice.dto.response;

import com.cimhub.common.model.Points;
import com.cimhub.tripservice.model.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
@Schema(name = "PassengerRideResponse", description = "Booking details as seen by the passenger")
public record PassengerRideResponse(
        @Schema(description = "Unique booking/ride identifier", example = "ride_001")
        String rideId,

        @Schema(description = "Trip identifier this booking belongs to", example = "trip_001")
        String tripId,

        @Schema(description = "Driver's user ID", example = "john.driver@example.com")
        String driverId,

        @Schema(description = "Vehicle license plate number", example = "HE-XX-1234")
        String vehicleNumber,

        @Schema(description = "Car type (e.g., SEDAN, SUV, VAN)", example = "SEDAN")
        String carType,

        @Schema(description = "Current booking status", example = "CONFIRMED")
        BookingStatus rideStatus,

        @Schema(description = "Passenger's pickup location")
        Points pickupLocation,

        @Schema(description = "Passenger's drop-off location")
        Points dropoffLocation,

        @Schema(description = "Trip departure date and time", example = "2026-01-11T13:09:26Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime tripStartDateTime,

        @Schema(description = "Timezone of the trip departure", example = "Europe/Berlin")
        String tripTimezone,

        @Schema(description = "Number of seats booked", example = "1")
        int bookedSeats,

        @Schema(description = "Distance of the passenger's ride segment in km", example = "45.2")
        Double rideDistanceInKm,

        @Schema(description = "Estimated duration of the ride segment in minutes", example = "35.0")
        Double rideDurationInMinutes,

        @Schema(description = "Price per seat for this ride in EUR", example = "15.50")
        Double pricePerSeat,

        @Schema(description = "Estimated fare for this ride in EUR", example = "15.50")
        Double estimatedFare,

        @Schema(description = "Details about the driver")
        DriverDetails driverDetails) {

    @Builder
    @Schema(name = "DriverDetails", description = "Driver information visible to passengers")
    public record DriverDetails(
            @Schema(description = "Driver's display name", example = "John D.")
            String name,

            @Schema(description = "Driver's average rating (1-5)", example = "4.8")
            Double rating,

            @Schema(description = "Total number of completed trips by the driver", example = "42")
            Integer totalTrips) {
    }
}
