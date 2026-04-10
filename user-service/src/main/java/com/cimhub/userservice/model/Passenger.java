package com.cimhub.userservice.model;

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
 * Passenger profile model that stores passenger-specific information.
 * Linked to base user by userId.
 */
@Data
@Document(collection = "passengers")
public class Passenger {
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

    private UserType userType = UserType.PASSENGER;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    private String createdBy;
}
