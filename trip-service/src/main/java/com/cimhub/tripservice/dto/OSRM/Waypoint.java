package com.cimhub.tripservice.dto.OSRM;

import java.util.List;

public record Waypoint(
        String hint,
        Double distance,
        String name,
        List<Double> location) {
}
