package com.cimhub.tripservice.dto.OSRM;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OsrmDtoRecordsTest {

    @Test
    void records_shouldRetainAssignedValues() {
        Geometry geometry = new Geometry("LineString", List.of(List.of(13.4, 52.5), List.of(13.5, 52.6)));
        Legs leg = new Legs(List.of("step-1"), 1200.0, 300.0, "summary", 1.0);
        Route route = new Route(geometry, List.of(leg), 1200.0, 300.0, "routability", 1.0);
        Waypoint waypoint = new Waypoint("hint", 2.0, "name", List.of(13.4, 52.5));
        OsrmResponseDTO response = new OsrmResponseDTO("Ok", List.of(route), List.of(waypoint));

        assertEquals("Ok", response.code());
        assertEquals(1, response.routes().size());
        assertEquals("LineString", response.routes().getFirst().geometry().type());
        assertEquals(1200.0, response.routes().getFirst().distance());
        assertEquals("summary", response.routes().getFirst().legs().getFirst().summary());
        assertEquals("name", response.waypoints().getFirst().name());
    }
}
