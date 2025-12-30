package com.singhv.tripservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.singhv.common.models.Points;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for searching/viewing trips.
 * Returns trip time in both original timezone and optionally in user's timezone
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripViewDTO {

    private String tripId;
    private String driverId;
    private String vehicleNumber;
    private Points sourceAddress;
    private Points destinationAddress;

    // Trip time in ORIGINAL timezone (where trip will occur)
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    private ZonedDateTime tripStartDateTime;

    // Original timezone where trip occurs
    private String tripTimezone;

    // Trip time converted to USER'S timezone (optional, for convenience)
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    private ZonedDateTime tripStartDateTimeInUserTimezone;

    // User's timezone (optional)
    private String userTimezone;

    private int offeredSeat;
    private int availableSeats;
    private String tripStatus;
    private double pricePerKm;
}

