package com.cimhub.common.dto.response;

/**
 * Represents the state of an API response.
 */
public enum ResponseState {
    SUCCESS,
    ERROR,
    WARNING; // Not in use

    public String value() {
        return name();
    }
}
