package com.cimhub.tripservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Read-only projection of the vehicle document stored in user-service's "vehicles" collection.
 * Used to validate vehicle ownership and fetch vehicle type during trip creation.
 */
@Data
@NoArgsConstructor
@Document(collection = "vehicles")
public class VehicleDocument {

    @Id
    private String id;

    private String userId;

    private String vehicleNumber;

    private String vehicleType;
}
