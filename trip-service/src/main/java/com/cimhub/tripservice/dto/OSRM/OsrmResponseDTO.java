package com.cimhub.tripservice.dto.OSRM;

import java.util.List;

public record OsrmResponseDTO(
        String code,
        List<Route> routes,
        List<Waypoint> waypoints) {
}
