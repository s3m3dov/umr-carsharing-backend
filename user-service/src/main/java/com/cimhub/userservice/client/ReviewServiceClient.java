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
public class ReviewServiceClient {

    @Value("${services.review.base-url}")
    private String reviewServiceBaseUrl;

    private final RestClient restClient;

    public void deleteUserData(String userId) {
        log.info("Sending request to review-service to delete user data: {}", userId);
        try {
            restClient
                    .delete()
                    .uri(reviewServiceBaseUrl + "/api/internal/reviews/users/{userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 404) {
                            log.warn("User {} data not found in review-service during deletion", userId);
                        } else {
                            throw new HttpClientErrorException(res.getStatusCode(), res.getStatusText());
                        }
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new RuntimeException("Review-service error: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("User {} data deleted from review-service", userId);
        } catch (Exception e) {
            log.error("Failed to delete user {} data from review-service: {}", userId, e.getMessage());
        }
    }
}
