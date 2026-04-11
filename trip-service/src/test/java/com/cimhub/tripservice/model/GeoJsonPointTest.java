package com.cimhub.tripservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class GeoJsonPointTest {

    @Test
    void of_shouldBuildPointWithGeoJsonCoordinateOrder() {
        GeoJsonPoint point = GeoJsonPoint.of(52.5, 13.4);

        assertEquals("Point", point.getType());
        assertEquals(List.of(13.4, 52.5), point.getCoordinates());
        assertEquals(13.4, point.getLongitude());
        assertEquals(52.5, point.getLatitude());
    }

    @Test
    void getters_whenCoordinatesMissing_shouldReturnNull() {
        GeoJsonPoint point =
                GeoJsonPoint.builder().type("Point").coordinates(List.of()).build();

        assertNull(point.getLongitude());
        assertNull(point.getLatitude());
    }

    @Test
    void getLatitude_whenOnlyLongitudePresent_shouldReturnNull() {
        GeoJsonPoint point =
                GeoJsonPoint.builder().type("Point").coordinates(List.of(13.4)).build();

        assertEquals(13.4, point.getLongitude());
        assertNull(point.getLatitude());
    }
}
