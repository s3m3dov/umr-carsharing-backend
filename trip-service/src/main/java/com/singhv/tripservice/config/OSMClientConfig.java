package com.singhv.tripservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OSMClientConfig {


    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://router.project-osrm.org/route/v1/driving/")
                .build();
    }
}
