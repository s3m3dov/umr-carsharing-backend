package com.cimhub.authservice.client;

import com.cimhub.authservice.dto.request.CreateProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {

    @PostMapping("/profile")
    void createUserProfile(@RequestBody CreateProfileRequest request);
}
