package com.cimhub.userservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Date;

/**
 * Base User model for the carsharing platform.
 * Uses JPA inheritance (SINGLE_TABLE) to support both relational and document databases.
 * Subclasses (Driver, Passenger) extend this with role-specific fields.
 * Stored in "users" collection/table.
 */
@Data
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class User {
    @Id
    private String userId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    @Column(unique = true)
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