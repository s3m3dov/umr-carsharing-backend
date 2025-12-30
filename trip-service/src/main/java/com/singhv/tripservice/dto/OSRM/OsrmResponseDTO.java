package com.singhv.tripservice.dto.OSRM;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OsrmResponseDTO {

    private String code;
    private List<Route> routes;
    private List<Waypoint> waypoints;
}
