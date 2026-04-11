package com.cimhub.tripservice.service.route;

import com.cimhub.tripservice.dto.osrm.OsrmResponseDTO;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
public class OsrmRouteClient {

    private final WebClient webClient;
    private final int timeoutSeconds;
    private final int retryMaxAttempts;
    private final long retryBackoffMillis;

    public OsrmRouteClient(
            WebClient webClient,
            @Value("${osrm.timeout-seconds:5}") int timeoutSeconds,
            @Value("${osrm.retry.max-attempts:3}") int retryMaxAttempts,
            @Value("${osrm.retry.backoff-millis:500}") long retryBackoffMillis) {
        this.webClient = webClient;
        this.timeoutSeconds = timeoutSeconds;
        this.retryMaxAttempts = retryMaxAttempts;
        this.retryBackoffMillis = retryBackoffMillis;
    }

    public Mono<OsrmResponseDTO> getRoute(double startLat, double startLon, double endLat, double endLon) {
        String coordinates = startLon + "," + startLat + ";" + endLon + "," + endLat;

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(coordinates)
                        .queryParam("overview", "full")
                        .queryParam("geometries", "geojson")
                        .build())
                .retrieve()
                .bodyToMono(OsrmResponseDTO.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retryWhen(Retry.backoff(retryMaxAttempts, Duration.ofMillis(retryBackoffMillis))
                        .filter(throwable -> throwable instanceof Exception)
                        .doBeforeRetry(signal ->
                                log.warn("OSRM request failed, retrying (attempt {})", signal.totalRetries() + 1)))
                .doOnError(e -> log.error("OSRM route request failed after retries: {}", e.getMessage()));
    }
}
