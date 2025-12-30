package com.singhv.tripservice.service.routes;

import com.singhv.tripservice.dto.OSRM.OsrmResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .bodyToMono(OsrmResponseDTO.class);
    }
}
