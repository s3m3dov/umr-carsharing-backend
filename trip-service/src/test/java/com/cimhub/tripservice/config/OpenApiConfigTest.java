package com.cimhub.tripservice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OpenApiConfigTest {

    @Test
    void tripServiceOpenAPI_shouldBuildExpectedOpenApiModel() {
        OpenApiConfig config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "serverUrl", "/trip-service-test");

        OpenAPI openAPI = config.tripServiceOpenAPI();

        assertNotNull(openAPI);
        assertEquals("Trip Service API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertEquals("/trip-service-test", openAPI.getServers().getFirst().getUrl());

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }
}
