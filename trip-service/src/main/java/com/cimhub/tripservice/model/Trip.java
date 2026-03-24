package com.cimhub.tripservice.model;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.dto.OSRM.Geometry;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.geo.Point;

import java.time.Instant;
import java.util.List;

/**
 * Trip model - stores trip information for the carsharing platform.
 * Uses JPA annotations for database-agnostic design.
 * MongoDB-specific geospatial indexing is retained for performance optimization.
 * Stored in "trips" collection/table.
 */
@Entity
@Table(name = "trips")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Trip {

    @Id
    private String tripId; // Remove @NotBlank - MongoDB auto-generates this

    @NotBlank
    private TripStatus tripStatus;

    @NotBlank
    private String vehicleNumber;

    @NotBlank
    private String driverId;

    @NotBlank
    private Points sourceAddress;

    @NotBlank
    private Points destinationAddress;

    // GeoJSON fields for geospatial queries
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point sourceLocation;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point destinationLocation;

    @Min(1L)
    private int totalSeats;

    @Min(0L)
    private int bookedSeats;

    // Store in UTC for consistency across timezones
    private Instant tripStartDateTimeUTC;

    // Store the timezone of the trip's source location (e.g., "Asia/Kolkata",
    // "Europe/Berlin")
    private String tripTimezone;

    // Route information from OSRM
    private Geometry routeGeometry; // Full route path as GeoJSON LineString
    private double routeDistance; // Distance in meters
    private double routeDuration; // Duration in seconds

    @PositiveOrZero
    private double pricePerKm;

    private List<ObjectId> joinedRidersId;
    @Builder.Default
    private Instant createdDate = Instant.now();
}
