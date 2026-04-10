package com.cimhub.userservice.model;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;

    @Positive
    private int age;

    private UserType userType = UserType.DRIVER;

    private String licenseNumber;
    private DriverStatus driverStatus = DriverStatus.PENDING;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    private String createdBy;

}
