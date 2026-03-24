package com.cimhub.userservice.model;

import com.cimhub.common.enums.UserType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Passenger model extending User.
 * Passengers do not have vehicle information.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
public class Passenger extends User {

    public Passenger() {
        super();
        this.setUserType(UserType.PASSENGER);
    }
}
