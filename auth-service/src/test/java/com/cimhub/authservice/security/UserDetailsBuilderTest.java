package com.cimhub.authservice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cimhub.authservice.model.User;
import com.cimhub.common.enums.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class UserDetailsBuilderTest {

    @Test
    void fromUser_shouldAssignDefaultRoleAuthority() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("hashed");

        UserDetails userDetails = UserDetailsBuilder.fromUser(user);

        assertEquals("user@example.com", userDetails.getUsername());
        assertEquals("hashed", userDetails.getPassword());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void fromUser_shouldAssignAdditionalRoleAuthority() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("hashed");

        UserDetails userDetails = UserDetailsBuilder.fromUser(user, UserType.ADMIN.name());

        assertEquals(
                "ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void fromUserWithType_shouldAssignRoleFromUserType() {
        User user = new User();
        user.setEmail("driver@example.com");
        user.setPassword("hashed");
        user.setUserType(UserType.DRIVER);

        UserDetails userDetails = UserDetailsBuilder.fromUserWithType(user);

        assertEquals(
                "ROLE_DRIVER", userDetails.getAuthorities().iterator().next().getAuthority());
    }
}
