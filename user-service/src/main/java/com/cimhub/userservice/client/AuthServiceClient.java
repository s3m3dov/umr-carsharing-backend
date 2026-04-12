package com.cimhub.userservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    @Value("${services.auth.base-url}")
    private String authServiceBaseUrl;

    private final RestClient restClient;

    public void deleteUser(String userId) {
        log.info("Sending request to auth-service to delete user: {}", userId);
        try {
            restClient
                    .delete()
                    .uri(authServiceBaseUrl + "/api/internal/users/{userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 404) {
                            log.warn("User {} not found in auth-service during deletion", userId);
                        } else {
                            throw new HttpClientErrorException(res.getStatusCode(), res.getStatusText());
                        }
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new RuntimeException("Auth-service error: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("User {} deleted from auth-service", userId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User {} already deleted or not found in auth-service", userId);
        } catch (Exception e) {
            log.error("Failed to delete user {} from auth-service: {}", userId, e.getMessage());
            // We don't throw exception here to avoid breaking the profile deletion flow,
            // but in a real-world app, we might want to handle this differently.
        }
    }
}
