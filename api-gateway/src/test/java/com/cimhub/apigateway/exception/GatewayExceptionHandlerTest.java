package com.cimhub.apigateway.exception;

import com.cimhub.common.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayExceptionHandlerTest {

    private GatewayExceptionHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        handler = new GatewayExceptionHandler(objectMapper);
    }

    @Test
    void handle_withUnauthorizedException_shouldReturn401WithUnauthorizedCode() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/trip-service/api/trips/offer").build());

        handler.handle(exchange, new UnauthorizedException("Missing Authorization header")).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());

        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals(401, json.get("status").asInt());
        assertEquals("UNAUTHORIZED", json.get("code").asText());
        assertEquals("Missing Authorization header", json.get("message").asText());
    }

    @Test
    void handle_withWebClient4xx_shouldForwardDownstreamResponseAsIs() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/trip-service/api/trips/offer").build());

        WebClientResponseException upstreamBadRequest = WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"code\":\"TRIP_VALIDATION_FAILED\",\"message\":\"invalid payload\"}"
                        .getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        handler.handle(exchange, upstreamBadRequest).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());

        // Gateway forwards the downstream response as-is without parsing/rebuilding
        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals("TRIP_VALIDATION_FAILED", json.get("code").asText());
        assertEquals("invalid payload", json.get("message").asText());
    }

    @Test
    void handle_withWebClient4xxEmptyBody_shouldProvideMinimalError() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/trip-service/api/trips/offer").build());

        WebClientResponseException upstreamConflict = WebClientResponseException.create(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                HttpHeaders.EMPTY,
                "".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        handler.handle(exchange, upstreamConflict).block();

        assertEquals(HttpStatus.CONFLICT, exchange.getResponse().getStatusCode());

        // When downstream has no body, gateway creates minimal error
        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals(409, json.get("status").asInt());
        assertEquals("Conflict", json.get("error").asText());
    }

    @Test
    void handle_withConnectException_shouldReturn503() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth-service/api/auth/signup").build());

        RuntimeException wrapped = new RuntimeException(
                "connect failed",
                new ConnectException("Connection refused"));

        handler.handle(exchange, wrapped).block();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());

        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals(503, json.get("status").asInt());
        assertEquals("SERVICE_UNAVAILABLE", json.get("code").asText());
        assertEquals("Upstream service unavailable", json.get("message").asText());
    }

    @Test
    void handle_withWebClient5xx_shouldForwardDownstreamError() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/user-service/api/users/123").build());

        WebClientResponseException downstream5xx = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "{\"code\":\"DATABASE_ERROR\",\"message\":\"Connection pool exhausted\"}"
                        .getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        handler.handle(exchange, downstream5xx).block();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());

        // 5xx errors from downstream should also be forwarded as-is
        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals("DATABASE_ERROR", json.get("code").asText());
        assertEquals("Connection pool exhausted", json.get("message").asText());
    }

    @Test
    void handle_withNoResourceFoundException_shouldReturn404() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/unknown").build());

        handler.handle(exchange, new org.springframework.web.reactive.resource.NoResourceFoundException("GET /api/unknown"))
                .block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());

        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals(404, json.get("status").asInt());
        assertEquals("RESOURCE_NOT_FOUND", json.get("code").asText());
    }

    @Test
    void handle_withIllegalArgumentException_shouldReturn400() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trips").build());

        handler.handle(exchange, new IllegalArgumentException("Invalid request parameter"))
                .block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());

        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals(400, json.get("status").asInt());
        assertEquals("BAD_REQUEST", json.get("code").asText());
        assertEquals("Invalid request parameter", json.get("message").asText());
    }

    @Test
    void handle_withUnexpectedException_shouldReturn500() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/trips").build());

        handler.handle(exchange, new NullPointerException("Unexpected null value"))
                .block();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());

        String body = exchange.getResponse().getBodyAsString().block();
        JsonNode json = objectMapper.readTree(body);
        assertEquals(500, json.get("status").asInt());
        assertEquals("INTERNAL_SERVER_ERROR", json.get("code").asText());
        assertEquals("Gateway error", json.get("message").asText());
    }
}
