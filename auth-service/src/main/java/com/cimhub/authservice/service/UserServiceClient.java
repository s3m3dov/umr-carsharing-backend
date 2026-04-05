package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.CreateProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    /**
     * Check if a user profile already exists in user-service
     * @param userId the user ID to check
     * @return true if profile exists, false if not found
     * @throws RuntimeException if service is unavailable or other errors occur
     */
    public boolean profileExists(String userId) {
        try {
            restClient.get()
                    .uri(userServiceBaseUrl + "/api/users/{userId}", userId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                        // Expected: user doesn't exist yet
                        throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
                    })
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.error("User service error checking profile: {}", res.getStatusText());
                        throw new RuntimeException("Failed to check user profile: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("Profile exists for userId: {}", userId);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("Profile does not exist for userId: {}", userId);
                return false;
            }
            throw e;
        } catch (Exception e) {
            log.error("Error checking if profile exists for userId {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to check user profile in user-service", e);
        }
    }

    /**
     * Create a user profile in user-service
     * Handles idempotency: if profile already exists (409), logs and continues
     * @param request the profile creation request
     * @throws RuntimeException if profile creation fails for reasons other than 409 Conflict
     */
    public void createProfile(CreateProfileRequest request) {
        try {
            restClient.post()
                    .uri(userServiceBaseUrl + "/api/users/profile")
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (req, res) -> {
                        // Profile already exists - not an error, log and continue
                        log.warn("Profile already exists for userId: {} - continuing with signup", request.userId());
                        throw new HttpClientErrorException(HttpStatus.CONFLICT);
                    })
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.error("User service error creating profile: {}", res.getStatusText());
                        throw new RuntimeException("Failed to create user profile: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("Successfully created user profile for userId: {}", request.userId());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                // Profile already exists, which is acceptable during signup retry
                log.info("Profile already exists for userId: {} - this is acceptable", request.userId());
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("Error calling user-service to create profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user profile in user-service", e);
        }
    }
}
