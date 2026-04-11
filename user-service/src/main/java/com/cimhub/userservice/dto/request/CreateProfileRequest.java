package com.cimhub.userservice.dto.request;

public interface CreateProfileRequest {
    String firstName();

    String lastName();

    String phoneNumber();

    int age();
}
