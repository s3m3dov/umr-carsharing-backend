package com.cimhub.common.util;

import com.cimhub.common.exception.ForbiddenException;

/**
 * Utility to enforce admin-only access on endpoints.
 * Services receive X-User-Role header from the API gateway after JWT validation.
 */
public final class AdminGuard {

    private AdminGuard() {
    }

    public static void requireAdmin(String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new ForbiddenException("Admin access required");
        }
    }
}
