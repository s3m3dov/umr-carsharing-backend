package com.cimhub.userservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class UserServiceApplicationTest {

    @Test
    void class_shouldBeAnnotatedAsSpringBootApplication() {
        assertTrue(UserServiceApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
