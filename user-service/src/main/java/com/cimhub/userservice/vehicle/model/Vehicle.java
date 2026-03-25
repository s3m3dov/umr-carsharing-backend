package com.cimhub.userservice.vehicle.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Vehicle model for storing vehicle information.
 * Uses MongoDB for persistence.
 * Stored in "vehicles" collection.
 */
@Data
@Document(collection = "vehicles")
@NoArgsConstructor
public class Vehicle {
    @Id
    private String id;

    @NotBlank
    private String userId;

    @NotBlank
    private String vehicleNumber;

    @NotBlank
    private String vehicleType;

    private String vehicleName;
    private String vehicleColor;
    private String seatingCapacity;
}