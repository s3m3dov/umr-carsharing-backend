package com.cimhub.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceApplicationTest {

    @Test
    void class_shouldBeAnnotatedAsSpringBootApplication() {
        assertTrue(UserServiceApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
