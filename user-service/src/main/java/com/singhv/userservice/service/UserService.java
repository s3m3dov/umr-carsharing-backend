package com.singhv.userservice.service;

import com.singhv.userservice.dto.LoginRequestDTO;
import com.singhv.userservice.dto.LoginResponseDTO;
import com.singhv.userservice.dto.SignUpResponseDTO;
import com.singhv.userservice.dto.UserInfoDTO;
import com.singhv.userservice.dto.UserProfileDTO;

public interface UserService {
    UserInfoDTO getUserInfo(String userId);

    void createUserProfile(UserProfileDTO profileDTO);

    SignUpResponseDTO registerNewUser(UserInfoDTO userInfo);

    LoginResponseDTO checkUserLogin(LoginRequestDTO requestContent);
}