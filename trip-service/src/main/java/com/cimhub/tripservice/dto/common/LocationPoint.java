package com.cimhub.tripservice.dto.common;

public record LocationPoint(
        Double latitude,
        Double longitude,
        String placeAddress) {
}
