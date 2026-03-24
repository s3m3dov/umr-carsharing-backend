package com.cimhub.vehicleservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vehicle model - stores vehicle information.
 * Uses JPA annotations for database-agnostic design (works with relational and document databases).
 * Stored in "vehicles" collection/table.
 */
@Data
@Entity
@Table(name = "vehicles")
@NoArgsConstructor
public class Vehicle {
    @NotBlank
    private String userId;

    @Id
    @NotBlank
    private String vehicleNumber;

    @NotBlank
    private String vehicleType;

    private String vehicleName;
    private String vehicleColor;
    private String seatingCapacity;
}