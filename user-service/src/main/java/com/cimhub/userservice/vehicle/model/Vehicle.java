package com.cimhub.userservice.vehicle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

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