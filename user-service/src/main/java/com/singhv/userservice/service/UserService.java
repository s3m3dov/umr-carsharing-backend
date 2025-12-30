package com.singhv.userservice.service;

import com.singhv.userservice.dto.UserInfoDTO;
import com.singhv.userservice.dto.UserProfileDTO;

public interface UserService {
    UserInfoDTO getUserInfo(String userId);
    void createUserProfile(UserProfileDTO profileDTO);
}