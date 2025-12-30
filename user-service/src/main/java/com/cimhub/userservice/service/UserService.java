package com.cimhub.userservice.service;

import com.cimhub.userservice.dto.LoginRequestDTO;
import com.cimhub.userservice.dto.LoginResponseDTO;
import com.cimhub.userservice.dto.SignUpResponseDTO;
import com.cimhub.userservice.dto.UserInfoDTO;
import com.cimhub.userservice.dto.UserProfileDTO;

public interface UserService {
    UserInfoDTO getUserInfo(String userId);

    void createUserProfile(UserProfileDTO profileDTO);

    SignUpResponseDTO registerNewUser(UserInfoDTO userInfo);

    LoginResponseDTO checkUserLogin(LoginRequestDTO requestContent);
}