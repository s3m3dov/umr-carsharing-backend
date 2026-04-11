package com.cimhub.common.util;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminGuardTest {

    @Test
    void requireAdmin_withAdminRole_shouldPass() {
        assertDoesNotThrow(() -> AdminGuard.requireAdmin(UserType.ADMIN.name()));
        assertDoesNotThrow(() -> AdminGuard.requireAdmin(UserType.ADMIN.name().toLowerCase(Locale.ROOT)));
    }

    @Test
    void requireAdmin_withNonAdminRole_shouldThrowForbidden() {
        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> AdminGuard.requireAdmin(UserType.DRIVER.name()));

        assertEquals("Admin access required", ex.getMessage());
    }

    @Test
    void requireAdmin_withPassengerRole_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> AdminGuard.requireAdmin(UserType.PASSENGER.name()));
    }

    @Test
    void requireAdmin_withNullRole_shouldThrowForbidden() {
        // null does not equal "ADMIN" — must be treated as unauthorized
        assertThrows(ForbiddenException.class, () -> AdminGuard.requireAdmin((String) null));
        assertThrows(ForbiddenException.class, () -> AdminGuard.requireAdmin((UserType) null));
    }

    @Test
    void requireAdmin_withEmptyRole_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> AdminGuard.requireAdmin(""));
    }

    @Test
    void requireAdmin_withArbitraryString_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> AdminGuard.requireAdmin("SUPERUSER"));
    }
}
