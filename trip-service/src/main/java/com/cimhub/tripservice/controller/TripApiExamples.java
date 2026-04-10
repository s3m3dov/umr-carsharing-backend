package com.cimhub.tripservice.controller;

import com.cimhub.tripservice.dto.examples.TripApiDoc;

/**
 * API examples for Trip endpoints.
 * @deprecated Use {@link TripApiDoc} instead.
 */
@Deprecated
public final class TripApiExamples {

    private TripApiExamples() {}

    public static final String OFFER_TRIP_RESPONSE = TripApiDoc.OFFER_TRIP_RESPONSE;
    public static final String CANCEL_TRIP_RESPONSE = TripApiDoc.CANCEL_TRIP_RESPONSE;
    public static final String SEARCH_TRIPS_RESPONSE = TripApiDoc.SEARCH_TRIPS_RESPONSE;
    public static final String ACTIVE_TRIPS_RESPONSE = TripApiDoc.ACTIVE_TRIPS_RESPONSE;
    public static final String ERROR_TRIP_NOT_FOUND = TripApiDoc.ERROR_TRIP_NOT_FOUND;
    public static final String ERROR_TRIP_IN_PROGRESS = TripApiDoc.ERROR_TRIP_IN_PROGRESS;
    public static final String ERROR_INVALID_TRIP_DATA = TripApiDoc.ERROR_INVALID_TRIP_DATA;
    public static final String ERROR_INVALID_COORDINATES = TripApiDoc.ERROR_INVALID_COORDINATES;
    public static final String ERROR_UNAUTHORIZED = TripApiDoc.ERROR_UNAUTHORIZED;
}