package com.cimhub.apigateway.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway.security")
@Getter
@Setter
public class SecurityProperties {
    private List<String> actuatorPaths = List.of("/actuator/**");
    private List<String> publicPaths = List.of();
    private List<String> docsPaths = List.of();
    private List<String> adminPaths = List.of();
    private List<String> internalPaths = List.of();
}
