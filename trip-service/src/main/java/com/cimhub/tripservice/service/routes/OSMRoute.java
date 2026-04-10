package com.cimhub.tripservice.service.routes;

import com.cimhub.tripservice.dto.osrm.OsrmResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OSMRoute {

    private final WebClient webClient;

    public Mono<OsrmResponseDTO> getRoute(double startLat, double startLon, double endLat, double endLon) {
        String coordinates = startLon + "," + startLat + ";" + endLon + "," + endLat;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(coordinates)
                        .queryParam("overview", "full")
                        .queryParam("geometries", "geojson")
                        .build())
                .retrieve()
                .bodyToMono(OsrmResponseDTO.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(throwable -> throwable instanceof Exception)
                        .doBeforeRetry(signal -> {
                            log.warn("OSRM request failed, retrying (attempt {})", signal.totalRetries() + 1);
                        }))
                .doOnError(e -> log.error("OSRM route request failed after retries: {}", e.getMessage()));
    }
}
