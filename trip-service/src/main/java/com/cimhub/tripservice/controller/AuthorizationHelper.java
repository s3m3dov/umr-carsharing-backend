package com.cimhub.tripservice.controller;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;

/**
 * Shared authorization checks for controller-layer role enforcement.
 */
public final class AuthorizationHelper {

    private AuthorizationHelper() {}

    public static void requireDriverRole(String userRole) {
        if (!UserType.DRIVER.name().equalsIgnoreCase(userRole)) {
            throw new ForbiddenException("Driver access required");
        }
    }

    public static void requirePassengerRole(String userRole) {
        if (!UserType.PASSENGER.name().equalsIgnoreCase(userRole)) {
            throw new ForbiddenException("Passenger access required");
        }
    }

    public static void requireDriverOrPassengerRole(String userRole) {
        if (!UserType.DRIVER.name().equalsIgnoreCase(userRole)
                && !UserType.PASSENGER.name().equalsIgnoreCase(userRole)) {
            throw new ForbiddenException("Driver or Passenger access required");
        }
    }
}
