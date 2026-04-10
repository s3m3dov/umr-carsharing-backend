package com.cimhub.userservice.vehicle.controller;

import com.cimhub.userservice.vehicle.dto.examples.VehicleApiDoc;

/**
 * API examples for Vehicle endpoints.
 * @deprecated Use {@link VehicleApiDoc} instead.
 */
@Deprecated
public final class VehicleApiExamples {

    private VehicleApiExamples() {}

    public static final String GET_VEHICLES_RESPONSE = VehicleApiDoc.GET_VEHICLES_RESPONSE;
    public static final String ERROR_NO_VEHICLES = VehicleApiDoc.ERROR_NO_VEHICLES;
    public static final String REGISTER_VEHICLE_RESPONSE = VehicleApiDoc.REGISTER_VEHICLE_RESPONSE;
    public static final String ERROR_INVALID_VEHICLE_DATA = VehicleApiDoc.ERROR_INVALID_VEHICLE_DATA;
    public static final String ERROR_VEHICLE_LIMIT = VehicleApiDoc.ERROR_VEHICLE_LIMIT;
}