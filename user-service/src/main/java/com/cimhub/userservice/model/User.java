package com.cimhub.userservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Base User model for the carsharing platform.
 * Subclasses (Driver, Passenger) extend this with role-specific fields.
 * MongoDB stores the actual type using the _class field for polymorphism.
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
    @Indexed(unique = true)
    private String email;

    private String phoneNumber;

    @NotNull
    private UserType userType;

    @Positive
    private int age;

    private Date dob;

    private Date createdDate = new Date();

    private String createdBy;

    public User() {
        this.createdDate = new Date();
    }

    public void initializeCreatedBy() {
        if (this.createdBy == null && this.email != null) {
            this.createdBy = this.email;
        }
    }
}