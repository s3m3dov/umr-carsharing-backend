package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.CreateProfileRequest;
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
public class UserServiceClient {

    @Value("${services.user.base-url}")
    private String userServiceBaseUrl;

    private final RestClient restClient;

    public void createProfile(CreateProfileRequest request) {
        try {
            restClient.post()
                    .uri(userServiceBaseUrl + "/api/users/profile")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.error("User service error creating profile: {}", res.getStatusText());
                        throw new RuntimeException("Failed to create user profile: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("Successfully created user profile for userId: {}", request.userId());
        } catch (Exception e) {
            log.error("Error calling user-service to create profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user profile in user-service", e);
        }
    }
}
