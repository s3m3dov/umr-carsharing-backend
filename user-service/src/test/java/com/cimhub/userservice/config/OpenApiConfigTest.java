package com.cimhub.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    void userServiceOpenAPI_shouldBuildConfiguredSpec() {
        OpenApiConfig config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "serverUrl", "/custom-user-service");

        OpenAPI openAPI = config.userServiceOpenAPI();

        assertEquals("User Service API", openAPI.getInfo().getTitle());
        assertEquals("/custom-user-service", openAPI.getServers().getFirst().getUrl());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication"));
        assertEquals("Bearer Authentication", openAPI.getSecurity().getFirst().keySet().iterator().next());
    }
}
