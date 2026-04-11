package com.cimhub.authservice.security;

import com.cimhub.authservice.model.User;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class UserDetailsBuilder {

    private static final String DEFAULT_ROLE = "USER";
    private static final String ROLE_PREFIX = "ROLE_";

    private UserDetailsBuilder() {}

    public static UserDetails fromUser(User user) {
        return fromUser(user, DEFAULT_ROLE);
    }

    public static UserDetails fromUser(User user, String additionalRole) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(buildAuthorities(additionalRole))
                .build();
    }

    public static UserDetails fromUserWithType(User user) {
        return fromUser(user, user.getUserType().name());
    }

    private static List<SimpleGrantedAuthority> buildAuthorities(String role) {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));
    }
}
