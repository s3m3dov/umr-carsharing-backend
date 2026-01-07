package com.cimhub.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway.services")
@Getter
@Setter
public class ServiceProperties {
    private AuthConfig auth;

    @Getter
    @Setter
    public static class AuthConfig {
        private String baseUrl;
        private String validatePath;

        public String getValidateUrl() {
            return baseUrl + validatePath;
        }
    }
}
