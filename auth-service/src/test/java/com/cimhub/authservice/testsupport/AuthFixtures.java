package com.cimhub.authservice.testsupport;

import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.model.User;
import com.cimhub.common.enums.UserType;
import java.time.Instant;

public final class AuthFixtures {

    private AuthFixtures() {}

    public static User user(String email, UserType userType) {
        User user = new User();
        user.setUserId(email);
        user.setEmail(email);
        user.setPassword("hashed");
        user.setUserType(userType);
        user.setCreatedAt(Instant.parse("2030-01-01T00:00:00Z"));
        user.setUpdatedAt(Instant.parse("2030-01-01T00:00:00Z"));
        return user;
    }

    public static RegisterUserRequest registerRequest(String email, UserType userType, String licenseNumber) {
        return new RegisterUserRequest("John", "Driver", email, "Password123!", userType, "+49 123", 30, licenseNumber);
    }
}
