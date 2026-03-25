package com.cimhub.common.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Points", description = "Geographic coordinates with optional place information")
public class Points {
    @Schema(description = "Latitude coordinate", example = "50.8154")
    private double latitude;

    @Schema(description = "Longitude coordinate", example = "8.6747")
    private double longitude;

    @Schema(description = "Google Place ID for the location", example = "")
    private String placeId = "";

    @Schema(description = "Human-readable address of the location", example = "Marburg, Germany")
    private String placeAddress = null;
}
