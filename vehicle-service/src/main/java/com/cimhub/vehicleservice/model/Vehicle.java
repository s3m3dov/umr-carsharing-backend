package com.cimhub.vehicleservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "vehicles")
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