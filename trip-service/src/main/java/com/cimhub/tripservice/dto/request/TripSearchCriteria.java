package com.cimhub.tripservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Encapsulates all parameters for a geospatial trip route search.
 */
@Schema(name = "TripSearchCriteria", description = "Parameters for searching trips matching a pickup and dropoff route")
public record TripSearchCriteria(

        @Schema(description = "Latitude of pickup location", example = "50.8154")
        double sourceLatitude,

        @Schema(description = "Longitude of pickup location", example = "8.6747")
        double sourceLongitude,

        @Schema(description = "Search radius around pickup in km", example = "5.0")
        double sourceRadiusKm,

        @Schema(description = "Latitude of dropoff location", example = "50.7265")
        double destinationLatitude,

        @Schema(description = "Longitude of dropoff location", example = "7.0895")
        double destinationLongitude,

        @Schema(description = "Search radius around dropoff in km", example = "5.0")
        double destinationRadiusKm,

        @Schema(description = "Earliest departure time (ISO 8601, optional)", example = "2024-03-25T08:00:00Z")
        String earliestDepartureTime,

        @Schema(description = "Latest departure time (ISO 8601, optional)", example = "2024-03-25T18:00:00Z")
        String latestDepartureTime,

        @Schema(description = "Number of seats the passenger needs", example = "2")
        Integer requestedSeats,

        @Schema(description = "Exclude trips created by this user ID (the searching passenger)")
        String excludeDriverId,

        @Schema(description = "Minimum price per seat", example = "5.0")
        Double minPrice,

        @Schema(description = "Maximum price per seat", example = "20.0")
        Double maxPrice,

        @Schema(description = "Car type filter (e.g., SEDAN, SUV)", example = "SEDAN")
        String carType,

        @Schema(description = "Minimum available seats on the trip", example = "2")
        Integer minAvailableSeats
) {}
