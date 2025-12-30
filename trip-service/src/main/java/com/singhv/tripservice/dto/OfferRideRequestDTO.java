package com.singhv.tripservice.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.singhv.common.models.Points;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfferRideRequestDTO {

    private String vehicleNumber;
    private Points sourceAddress;
    private Points destinationAddress;

    // Accept time with timezone information in ISO-8601 format
    // Example: "2025-10-12T14:30:00+05:30" for India or "2025-10-12T14:30:00+02:00" for Germany
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    private ZonedDateTime tripStartDateTime;

    private int offeredSeat;

}
