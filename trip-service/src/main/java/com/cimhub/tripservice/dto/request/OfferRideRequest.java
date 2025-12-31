package com.cimhub.tripservice.dto.request;

import com.cimhub.common.models.Points;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record OfferRideRequest(
        String driverId,
        String vehicleNumber,
        Points sourceAddress,
        Points destinationAddress,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime tripStartDateTime,

        int offeredSeat) {
}
