package com.cimhub.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.tokens")
public class TokenProperties {

    private long resetTokenExpiryMs = 3600000L;
    private long verificationTokenExpiryMs = 86400000L;
}
