package com.cimhub.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for JWT identity headers injected by the gateway.
 * Used for centralizing header names and avoiding raw strings in logic.
 */
@Getter
@RequiredArgsConstructor
public enum RequestHeaderName {
    USER_ID("X-User-Id"),
    USER_EMAIL("X-User-Email"),
    USER_ROLE("X-User-Role");

    private final String headerName;

    @Override
    public String toString() {
        return headerName;
    }
}
