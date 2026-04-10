package com.cimhub.tripservice.model;

import com.cimhub.common.model.Points;
import com.cimhub.tripservice.dto.osrm.Geometry;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.geo.Point;

import java.time.Instant;
import java.util.List;

/**
 * Trip model - stores trip information for the carsharing platform.
 * Uses MongoDB for persistence.
 * Stored in "trips" collection.
 */
@Document(collection = "trips")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Trip {

    @Id
    private String tripId;

    @NotBlank
    private TripStatus tripStatus;

    @NotBlank
    private String vehicleNumber;

    @NotBlank
    private String carType;

    @NotBlank
    private String driverId;

    @NotBlank
    private Points sourceAddress;

    @NotBlank
    private Points destinationAddress;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point sourceLocation;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point destinationLocation;

    @Min(1L)
    private int totalSeats;

    @Min(0L)
    private int bookedSeats;

    private Instant tripStartDateTimeUTC;

    private String tripTimezone;

    private Geometry routeGeometry;
    private double routeDistance;
    private double routeDuration;

    @PositiveOrZero
    private double pricePerSeat;

    private List<ObjectId> joinedBookingIds;
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
