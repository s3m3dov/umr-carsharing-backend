package com.cimhub.common.constant;

import com.cimhub.common.enums.RequestHeaderName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Central source of truth for HTTP header names.
 * Uses string literals for annotation compatibility (e.g. @RequestHeader).
 * Strings must match RequestHeaderName enum values.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderConstants {
    // Identity Headers
    public static final String X_USER_ID = "X-User-Id";      // Matches RequestHeaderName.USER_ID
    public static final String X_USER_EMAIL = "X-User-Email"; // Matches RequestHeaderName.USER_EMAIL
    public static final String X_USER_ROLE = "X-User-Role";   // Matches RequestHeaderName.USER_ROLE
    
    // Auth Prefix
    public static final String BEARER_PREFIX = "Bearer ";
}
