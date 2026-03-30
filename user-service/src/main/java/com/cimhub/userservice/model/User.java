package com.cimhub.userservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * User model for the carsharing platform.
 * Uses MongoDB for persistence.
 * Stored in "users" collection.
 */
@Data
@Document(collection = "users")
public class User {
    @Id
    private String userId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;

    @NotNull
    private UserType userType;

    @Positive
    private int age;

    private Instant dob;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    private String createdBy;

    public User() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void initializeCreatedBy() {
        if (this.createdBy == null && this.email != null) {
            this.createdBy = this.email;
        }
    }
}
