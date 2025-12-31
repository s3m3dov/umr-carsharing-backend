package com.cimhub.tripservice.dto.OSRM;

import java.util.List;

public record Legs(
        List<Object> steps,
        Double distance,
        Double duration,
        String summary,
        Double weight) {
}
