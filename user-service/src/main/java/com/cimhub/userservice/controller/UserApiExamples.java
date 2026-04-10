package com.cimhub.userservice.controller;

import com.cimhub.userservice.dto.examples.UserApiDoc;

/**
 * API examples for User endpoints.
 * @deprecated Use {@link UserApiDoc} instead.
 */
@Deprecated
public final class UserApiExamples {

    private UserApiExamples() {}

    public static final String GET_USER_RESPONSE = UserApiDoc.GET_USER_RESPONSE;
    public static final String ERROR_USER_NOT_FOUND = UserApiDoc.ERROR_USER_NOT_FOUND;
}