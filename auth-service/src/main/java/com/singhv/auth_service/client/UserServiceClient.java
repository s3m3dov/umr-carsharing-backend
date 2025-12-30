package com.singhv.auth_service.client;

import com.singhv.auth_service.dto.UserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {

    @PostMapping("/profile")
    void createUserProfile(@RequestBody UserProfileDTO userProfileDTO);
}

