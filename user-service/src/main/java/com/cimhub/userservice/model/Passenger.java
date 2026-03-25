package com.cimhub.userservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Passenger model extending User.
 * Passengers do not have vehicle information.
 * Stored in "users" table (inherited from User via JPA SINGLE_TABLE inheritance).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Passenger extends User {

    public Passenger() {
        super();
        this.setUserType(UserType.PASSENGER);
    }
}
