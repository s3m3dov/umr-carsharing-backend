package com.cimhub.tripservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OSMClientConfigTest {

    @Test
    void beans_shouldCreateWebClientBuilderAndWebClient() {
        OSMClientConfig config = new OSMClientConfig();

        WebClient.Builder builder = config.webClientBuilder();
        WebClient webClient = config.webClient(builder);

        assertNotNull(builder);
        assertNotNull(webClient);
    }
}
