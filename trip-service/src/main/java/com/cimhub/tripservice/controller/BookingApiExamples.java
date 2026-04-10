package com.cimhub.tripservice.controller;

import com.cimhub.tripservice.dto.examples.BookingApiDoc;

/**
 * API examples for Booking endpoints.
 * @deprecated Use {@link BookingApiDoc} instead.
 */
@Deprecated
public final class BookingApiExamples {

    private BookingApiExamples() {}

    public static final String JOIN_TRIP_RESPONSE = BookingApiDoc.JOIN_TRIP_RESPONSE;
    public static final String CANCEL_BOOKING_RESPONSE = BookingApiDoc.CANCEL_BOOKING_RESPONSE;
    public static final String ACTIVE_BOOKINGS_RESPONSE = BookingApiDoc.ACTIVE_BOOKINGS_RESPONSE;
    public static final String ERROR_TRIP_FULL = BookingApiDoc.ERROR_TRIP_FULL;
    public static final String ERROR_TRIP_NOT_FOUND = BookingApiDoc.ERROR_TRIP_NOT_FOUND;
    public static final String ERROR_BOOKING_NOT_FOUND = BookingApiDoc.ERROR_BOOKING_NOT_FOUND;
    public static final String ERROR_INVALID_BOOKING_DATA = BookingApiDoc.ERROR_INVALID_BOOKING_DATA;
    public static final String ERROR_TRIP_IN_PROGRESS = BookingApiDoc.ERROR_TRIP_IN_PROGRESS;
}