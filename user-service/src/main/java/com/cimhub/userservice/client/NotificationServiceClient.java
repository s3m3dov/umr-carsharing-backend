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
public class NotificationServiceClient {

    @Value("${services.notification.base-url}")
    private String notificationServiceBaseUrl;

    private final RestClient restClient;

    public void deleteUserData(String userId) {
        log.info("Sending request to notification-service to delete user data: {}", userId);
        try {
            restClient
                    .delete()
                    .uri(notificationServiceBaseUrl + "/api/internal/notifications/users/{userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 404) {
                            log.warn("User {} data not found in notification-service during deletion", userId);
                        } else {
                            throw new HttpClientErrorException(res.getStatusCode(), res.getStatusText());
                        }
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new RuntimeException("Notification-service error: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("User {} data deleted from notification-service", userId);
        } catch (Exception e) {
            log.error("Failed to delete user {} data from notification-service: {}", userId, e.getMessage());
        }
    }
}
