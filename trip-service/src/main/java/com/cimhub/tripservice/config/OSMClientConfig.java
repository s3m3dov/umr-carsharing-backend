package com.cimhub.tripservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class OSMClientConfig {

    private static final int OSRM_CONNECT_TIMEOUT_MS = 5000;
    private static final int OSRM_IO_TIMEOUT_SECONDS = 5;
    private static final int MAX_IN_MEMORY_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, OSRM_CONNECT_TIMEOUT_MS)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(OSRM_IO_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(OSRM_IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)));

        return webClientBuilder
                .baseUrl("https://router.project-osrm.org/route/v1/driving/")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(MAX_IN_MEMORY_SIZE_BYTES))
                        .build())
                .build();
    }
}
