package com.cimhub.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "auth.security")
@Getter
@Setter
public class SecurityProperties {
    private List<String> actuatorPaths = List.of();
    private List<String> publicPaths = List.of();
    private List<String> docsPaths = List.of();
}
