package com.cimhub.tripservice.model;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GeoJSON Point representation for MongoDB geospatial queries
 * Format: { "type": "Point", "coordinates": [longitude, latitude] }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeoJsonPoint {

    @Builder.Default
    private String type = "Point";

    private List<Double> coordinates; // [longitude, latitude]

    /**
     * Create a GeoJSON Point from latitude and longitude
     *
     * @param latitude  Latitude value
     * @param longitude Longitude value
     * @return GeoJsonPoint instance
     */
    public static GeoJsonPoint of(double latitude, double longitude) {
        return GeoJsonPoint.builder()
                .type("Point")
                .coordinates(Arrays.asList(longitude, latitude)) // GeoJSON uses [lon, lat]
                .build();
    }

    /**
     * Get longitude from coordinates
     */
    public Double getLongitude() {
        return coordinates != null && coordinates.size() >= 1 ? coordinates.get(0) : null;
    }

    /**
     * Get latitude from coordinates
     */
    public Double getLatitude() {
        return coordinates != null && coordinates.size() >= 2 ? coordinates.get(1) : null;
    }
}
