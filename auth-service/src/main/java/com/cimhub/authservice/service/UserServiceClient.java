package com.cimhub.authservice.service;

import java.util.HashMap;
import java.util.Map;
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

    public void createPassengerSubProfile(
            String userId, String firstName, String lastName, String phoneNumber, int age) {
        try {
            restClient
                    .post()
                    .uri(userServiceBaseUrl + "/api/internal/passengers/{userId}", userId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(buildPassengerBody(firstName, lastName, phoneNumber, age))
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (req, res) -> {
                        throw new HttpClientErrorException(HttpStatus.CONFLICT);
                    })
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RuntimeException("Failed to create passenger sub-profile: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("Passenger sub-profile created for userId: {}", userId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.warn("Passenger sub-profile already exists for userId: {}", userId);
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("Error creating passenger sub-profile for userId {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to create passenger sub-profile", e);
        }
    }

    public void createDriverSubProfile(
            String userId, String firstName, String lastName, String phoneNumber, int age, String licenseNumber) {
        try {
            restClient
                    .post()
                    .uri(userServiceBaseUrl + "/api/internal/drivers/{userId}", userId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(buildDriverBody(firstName, lastName, phoneNumber, age, licenseNumber))
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (req, res) -> {
                        throw new HttpClientErrorException(HttpStatus.CONFLICT);
                    })
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RuntimeException("Failed to create driver sub-profile: " + res.getStatusText());
                    })
                    .toBodilessEntity();
            log.info("Driver sub-profile created for userId: {}", userId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.warn("Driver sub-profile already exists for userId: {}", userId);
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("Error creating driver sub-profile for userId {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to create driver sub-profile", e);
        }
    }

    private Map<String, Object> buildDriverBody(
            String firstName, String lastName, String phoneNumber, int age, String licenseNumber) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("phoneNumber", phoneNumber);
        body.put("age", age);
        body.put("licenseNumber", licenseNumber);
        return body;
    }

    private Map<String, Object> buildPassengerBody(String firstName, String lastName, String phoneNumber, int age) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("phoneNumber", phoneNumber);
        body.put("age", age);
        return body;
    }
}
