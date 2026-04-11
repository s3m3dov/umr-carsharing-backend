package com.cimhub.common.util;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;

public final class OwnershipGuard {

    private OwnershipGuard() {
    }

    public static void requireOwnerOrAdmin(String authenticatedUserId, String targetUserId, UserType userRole) {
        if (isAdmin(userRole)) {
            return;
        }
        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            throw new ForbiddenException("Authenticated user id is required");
        }
        if (!authenticatedUserId.equalsIgnoreCase(targetUserId)) {
            throw new ForbiddenException("Access denied for requested user");
        }
    }

    public static void requireRoleOrAdmin(UserType userRole, UserType requiredRole) {
        if (isAdmin(userRole)) {
            return;
        }
        if (requiredRole == null || userRole == null || !requiredRole.equals(userRole)) {
            throw new ForbiddenException(requiredRole + " role required");
        }
    }

    private static boolean isAdmin(UserType userRole) {
        return UserType.ADMIN.equals(userRole);
    }
}
