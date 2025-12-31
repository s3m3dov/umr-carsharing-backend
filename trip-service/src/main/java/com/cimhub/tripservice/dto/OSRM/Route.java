package com.cimhub.tripservice.dto.OSRM;

import java.util.List;

public record Route(
        Geometry geometry,
        List<Legs> legs,
        Double distance,
        Double duration,
        String weight_name,
        Double weight) {
}
