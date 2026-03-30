package com.cimhub.userservice.dto.request;

public record UpdatePassengerRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        Integer age) {
}
