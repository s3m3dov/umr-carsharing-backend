package com.cimhub.tripservice.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LocationPoint", description = "Geographic location with coordinates and address")
public record LocationPoint(
        @Schema(description = "Latitude coordinate", example = "50.8154")
        Double latitude,

        @Schema(description = "Longitude coordinate", example = "8.6747")
        Double longitude,

        @Schema(description = "Human-readable address of the location", example = "Marburg, Germany")
        String placeAddress) {
}
