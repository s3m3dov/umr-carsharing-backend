package com.singhv.tripservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.singhv.common.models.Points;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Rider {

    @Id
    private String riderId;
    @NotBlank
    private String userId;

    private String allocatedTripId;
    private String allocatedDriverId;
    private String allocatedVehicleNumber;
    private String rideStatus = "REQUESTED";
    @NotBlank
    private Points sourceAddress;
    @NotBlank
    private Points destinationAddress;
    @NotBlank
    private Date riderStartTime;
    private double rideDistance;
    private @Min(1L) int requestedSeats = 1;
    private Date createdDate = new Date();
    private @PositiveOrZero double pricePerKm;
}
