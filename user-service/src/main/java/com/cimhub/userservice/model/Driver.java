package com.cimhub.userservice.model;

import com.cimhub.common.enums.DriverStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

/**
 * Driver profile model that stores driver-specific information.
 * Linked to base user by userId.
 */
@Data
@Document(collection = "drivers")
public class Driver {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String licenseNumber;
    private DriverStatus driverStatus = DriverStatus.PENDING;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    private String createdBy;

}
