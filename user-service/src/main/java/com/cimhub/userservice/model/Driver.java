package com.cimhub.userservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Driver model extending User with vehicle-specific information.
 * Only drivers have vehicle details.
 * Stored in "users" table (inherited from User via JPA SINGLE_TABLE inheritance).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Driver extends User {
    private String vehicleModel;
    private String licensePlate;

    public Driver() {
        super();
        this.setUserType(UserType.DRIVER);
    }
}
