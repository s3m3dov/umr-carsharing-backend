package com.cimhub.tripservice.dto.osrm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Route(
        Geometry geometry,
        List<Legs> legs,
        Double distance,
        Double duration,
        @JsonProperty("weight_name") String weightName,
        Double weight) {
}