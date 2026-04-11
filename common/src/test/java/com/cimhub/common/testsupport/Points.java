package com.cimhub.common.testsupport;

import java.util.List;

public final class Points {

    private Points() {
    }

    public static com.cimhub.common.model.Points berlin() {
        return of(52.5200, 13.4050, "Berlin");
    }

    public static com.cimhub.common.model.Points munich() {
        return of(48.1351, 11.5820, "Munich");
    }

    public static List<com.cimhub.common.model.Points> between(com.cimhub.common.model.Points source,
                                                                com.cimhub.common.model.Points destination) {
        return List.of(source, destination);
    }

    public static com.cimhub.common.model.Points of(double latitude, double longitude, String placeAddress) {
        com.cimhub.common.model.Points point = new com.cimhub.common.model.Points();
        point.setLatitude(latitude);
        point.setLongitude(longitude);
        point.setPlaceAddress(placeAddress);
        return point;
    }
}
