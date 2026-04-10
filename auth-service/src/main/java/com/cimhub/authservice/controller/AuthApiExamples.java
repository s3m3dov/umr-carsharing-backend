package com.cimhub.authservice.controller;

import com.cimhub.authservice.dto.examples.AuthApiDoc;

/**
 * API examples for Auth endpoints.
 * @deprecated Use {@link AuthApiDoc} instead.
 */
@Deprecated
public final class AuthApiExamples {

    private AuthApiExamples() {}

    public static final String SIGNUP_RESPONSE = AuthApiDoc.SIGNUP_RESPONSE;
    public static final String LOGIN_RESPONSE = AuthApiDoc.LOGIN_RESPONSE;
    public static final String VALIDATE_TOKEN_RESPONSE = AuthApiDoc.VALIDATE_TOKEN_RESPONSE;
    public static final String ERROR_VALIDATION = AuthApiDoc.ERROR_VALIDATION;
    public static final String ERROR_USER_EXISTS = AuthApiDoc.ERROR_USER_EXISTS;
    public static final String ERROR_INVALID_CREDENTIALS = AuthApiDoc.ERROR_INVALID_CREDENTIALS;
    public static final String ERROR_INVALID_TOKEN = AuthApiDoc.ERROR_INVALID_TOKEN;
}