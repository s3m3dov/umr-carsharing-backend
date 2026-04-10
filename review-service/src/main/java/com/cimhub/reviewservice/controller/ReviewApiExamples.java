package com.cimhub.reviewservice.controller;

import com.cimhub.reviewservice.dto.examples.ReviewApiDoc;

/**
 * API examples for Review endpoints.
 * @deprecated Use {@link ReviewApiDoc} instead.
 */
@Deprecated
public final class ReviewApiExamples {

    private ReviewApiExamples() {}

    public static final String SUBMIT_DRIVER_REVIEW_RESPONSE = ReviewApiDoc.SUBMIT_DRIVER_REVIEW_RESPONSE;
    public static final String SUBMIT_PASSENGER_REVIEW_RESPONSE = ReviewApiDoc.SUBMIT_PASSENGER_REVIEW_RESPONSE;
    public static final String GET_DRIVER_REVIEWS_RESPONSE = ReviewApiDoc.GET_DRIVER_REVIEWS_RESPONSE;
    public static final String GET_PASSENGER_REVIEWS_RESPONSE = ReviewApiDoc.GET_PASSENGER_REVIEWS_RESPONSE;
    public static final String GET_AVERAGE_RATING_RESPONSE = ReviewApiDoc.GET_AVERAGE_RATING_RESPONSE;
    public static final String ERROR_TRIP_NOT_COMPLETED = ReviewApiDoc.ERROR_TRIP_NOT_COMPLETED;
    public static final String ERROR_REVIEW_ALREADY_EXISTS = ReviewApiDoc.ERROR_REVIEW_ALREADY_EXISTS;
    public static final String ERROR_DRIVER_NOT_FOUND = ReviewApiDoc.ERROR_DRIVER_NOT_FOUND;
    public static final String ERROR_PASSENGER_NOT_FOUND = ReviewApiDoc.ERROR_PASSENGER_NOT_FOUND;
    public static final String ERROR_UNAUTHORIZED_REVIEWER = ReviewApiDoc.ERROR_UNAUTHORIZED_REVIEWER;
}