package com.cimhub.tripservice.service.route;

import com.cimhub.tripservice.dto.osrm.OsrmResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsrmRouteClientTest {

    @Test
    void getRoute_shouldBuildExpectedUriAndDeserializeResponse() {
        List<URI> capturedUris = new ArrayList<>();
        String json = """
                {
                  "code":"Ok",
                  "routes":[{
                    "geometry":{"type":"LineString","coordinates":[[13.4,52.5],[13.5,52.6]]},
                    "legs":[],
                    "distance":1200.0,
                    "duration":300.0,
                    "weight_name":"routability",
                    "weight":1.0
                  }],
                  "waypoints":[{
                    "hint":"hint-1",
                    "distance":0.0,
                    "name":"Start",
                    "location":[13.4,52.5]
                  }]
                }
                """;

        WebClient webClient = WebClient.builder()
                .baseUrl("https://router.project-osrm.org/route/v1/driving/")
                .exchangeFunction(request -> {
                    capturedUris.add(request.url());
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(json)
                            .build());
                })
                .build();

        OsrmRouteClient osmRoute = new OsrmRouteClient(webClient, 5, 3, 500);

        OsrmResponseDTO response = osmRoute.getRoute(52.5, 13.4, 52.6, 13.5).block();

        assertNotNull(response);
        assertEquals("Ok", response.code());
        assertEquals(1, response.routes().size());
        assertEquals(1200.0, response.routes().getFirst().distance());
        assertEquals(300.0, response.routes().getFirst().duration());

        assertEquals(1, capturedUris.size());
        String uri = capturedUris.getFirst().toString();
        assertTrue(uri.contains("/13.4,52.5;13.5,52.6"));
        assertTrue(uri.contains("overview=full"));
        assertTrue(uri.contains("geometries=geojson"));
    }
}
