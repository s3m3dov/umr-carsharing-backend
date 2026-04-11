package com.cimhub.tripservice.dto.osrm;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "Geometry", description = "GeoJSON geometry object from OSRM route response")
public record Geometry(
        @Schema(description = "GeoJSON geometry type", example = "LineString")
        String type,

        @Schema(description = "Array of [longitude, latitude] coordinate pairs defining the route")
        List<List<Double>> coordinates) {}
