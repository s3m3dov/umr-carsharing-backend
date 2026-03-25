package com.cimhub.tripservice.model;

import com.cimhub.common.models.Points;
import jakarta.validation.constraints.Min;
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

    private String passengerId;

    private String tripId;
    private String driverId;
    private String vehicleNumber;

    private Points pickupLocation;
    private Points dropoffLocation;

    @Min(1)
    private int requestedSeats;
    private BookingStatus status;

    private Instant rideStartTimeUTC;
    private String rideTimezone;

    private double estimatedPrice;
    private double pricePerSeat;

    private Instant createdAt;
    private Instant updatedAt;
}