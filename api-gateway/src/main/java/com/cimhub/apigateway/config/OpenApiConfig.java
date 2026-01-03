package com.cimhub.apigateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public CommandLineRunner openApiGroups(RouteDefinitionLocator locator,
            SwaggerUiConfigParameters swaggerUiConfigParameters) {
        return args -> locator.getRouteDefinitions().collectList().subscribe(definitions -> {
            definitions.stream()
                    .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
                    .filter(routeDefinition -> !routeDefinition.getId().contains("-docs"))
                    .forEach(routeDefinition -> {
                        String serviceName = routeDefinition.getId(); // e.g., user-service
                        String displayName = serviceName.replaceAll("-service", "");

                        AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                                displayName,
                                "/" + serviceName + "/v3/api-docs",
                                displayName);
                        swaggerUiConfigParameters.getUrls().add(swaggerUrl);
                    });
        });
    }
}
