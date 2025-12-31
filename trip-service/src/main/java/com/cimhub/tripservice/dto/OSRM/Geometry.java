package com.cimhub.tripservice.dto.OSRM;

import java.util.List;

public record Geometry(
        String type,
        List<List<Double>> coordinates) {
}
