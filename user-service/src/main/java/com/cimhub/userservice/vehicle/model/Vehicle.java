package com.cimhub.userservice.vehicle.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Vehicle model for storing vehicle information.
 * Uses MongoDB for persistence.
 * Stored in "vehicles" collection.
 */
@Getter
@Setter
@Document(collection = "vehicles")
@NoArgsConstructor
public class Vehicle {
    @Id
    private String id;

    @NotBlank
    private String userId;

    @NotBlank
    @Indexed(unique = true)
    private String vehicleNumber;

    @NotBlank
    private String vehicleType;

    private String vehicleName;
    private String vehicleColor;
    private String seatingCapacity;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
