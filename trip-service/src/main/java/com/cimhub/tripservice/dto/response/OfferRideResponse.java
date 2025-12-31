package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.dto.OSRM.Geometry;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record OfferRideResponse(
        String tripId,
        String vehicleNumber,
        Points sourceAddress,
        Points destinationAddress,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTime,

        String tripTimezone,
        Geometry routeGeometry,
        Double routeDistanceInMeters,
        Double routeDistanceInKm,
        Double routeDurationInSeconds,
        Double routeDurationInMinutes,
        Boolean tripCreated,
        String errorMessage) {
}
