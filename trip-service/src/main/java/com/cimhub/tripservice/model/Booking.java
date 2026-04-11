package com.cimhub.tripservice.model;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.model.Points;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Booking entity - represents a passenger's seat reservation on a trip.
 * Uses MongoDB for persistence.
 * Stored in "bookings" collection.
 */
@Document(collection = "bookings")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Booking {

    @Id
    private String bookingId;

    @NotBlank
    private String passengerId;

    @NotBlank
    private String tripId;
    
    @NotBlank
    private String driverId;
    
    private String vehicleNumber;

    @NotNull
    private Points pickupLocation;
    
    @NotNull
    private Points dropoffLocation;

    @Min(1)
    private int requestedSeats;
    
    @NotNull
    private BookingStatus status;

    private Instant rideStartTimeUTC;
    private String rideTimezone;

    private double estimatedPrice;
    private double pricePerSeat;

    private Instant createdAt;
    private Instant updatedAt;
}
