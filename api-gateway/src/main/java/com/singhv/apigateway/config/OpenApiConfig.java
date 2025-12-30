package com.singhv.apigateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class OpenApiConfig {

    @Bean
    public CommandLineRunner openApiGroups(RouteDefinitionLocator locator,
            SwaggerUiConfigParameters swaggerUiConfigParameters) {
        return args -> Objects.requireNonNull(locator.getRouteDefinitions().collectList().block())
                .stream()
                .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
                // Exclude the docs routes themselves
                .filter(routeDefinition -> !routeDefinition.getId().contains("-docs"))
                .forEach(routeDefinition -> {
                    String name = routeDefinition.getId().replaceAll("-service", "");

                    AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                            name,
                            "/v3/api-docs/" + name,
                            name);
                    swaggerUiConfigParameters.getUrls().add(swaggerUrl);
                });
    }
}
