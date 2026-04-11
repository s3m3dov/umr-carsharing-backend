package com.cimhub.tripservice.dto.osrm;

import java.util.List;

public record OsrmResponseDTO(String code, List<Route> routes, List<Waypoint> waypoints) {}
