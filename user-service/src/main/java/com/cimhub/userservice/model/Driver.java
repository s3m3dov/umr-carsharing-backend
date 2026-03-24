package com.cimhub.userservice.model;

import com.cimhub.common.enums.UserType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Driver model extending User with vehicle-specific information.
 * Only drivers have vehicle details.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
public class Driver extends User {
    private String vehicleModel;
    private String licensePlate;

    public Driver() {
        super();
        this.setUserType(UserType.DRIVER);
    }
}
