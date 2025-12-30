package com.cimhub.userservice.service.impl;

import com.cimhub.userservice.dto.LoginRequestDTO;
import com.cimhub.userservice.dto.LoginResponseDTO;
import com.cimhub.userservice.dto.SignUpResponseDTO;
import com.cimhub.userservice.dto.UserInfoDTO;
import com.cimhub.userservice.dto.UserProfileDTO;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.UserRepository;
import com.cimhub.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void createUserProfile(UserProfileDTO profileDTO) {
        log.info("Creating user profile for userId: {}", profileDTO.getUserId());

        // Check if user already exists
        User existingUser = userRepository.findByUserId(profileDTO.getUserId());
        if (existingUser != null) {
            throw new RuntimeException("User profile already exists for userId: " + profileDTO.getUserId());
        }

        // Create new user profile
        User newUser = new User();
        BeanUtils.copyProperties(profileDTO, newUser);
        newUser.setCreatedBy(profileDTO.getEmailId());
        newUser.setCreatedDate(new java.util.Date());

        userRepository.save(newUser);
        log.info("User profile created successfully for userId: {}", profileDTO.getUserId());
    }

    @Override
    public UserInfoDTO getUserInfo(String userId) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        User foundUser = userRepository.findByUserId(userId);

        if (foundUser == null) {
            throw new RuntimeException("User not found with userId: " + userId);
        }

        BeanUtils.copyProperties(foundUser, userInfoDTO);
        return userInfoDTO;
    }

    @Override
    public SignUpResponseDTO registerNewUser(UserInfoDTO userInfo) {
        SignUpResponseDTO responseDTO = new SignUpResponseDTO();
        User newUser = new User();
        BeanUtils.copyProperties(userInfo, newUser);
        newUser.setUserId(userInfo.getEmailId().toLowerCase());
        log.info("RegisterNewUser-->" + newUser);
        userRepository.save(newUser);
        User user = userRepository.findByUserId(userInfo.getEmailId().toLowerCase());
        responseDTO.setSignUpSuccess(true);
        responseDTO.setUserId(user.getUserId());
        responseDTO.setUsername(user.getFullName());
        return responseDTO;
    }

    @Override
    public LoginResponseDTO checkUserLogin(LoginRequestDTO requestContent) {
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        User userData = userRepository.findByUserId(requestContent.getEmailId().toLowerCase());
        log.info("LoginUserCheck --> " + userData);
        if (userData != null) {
            if (userData.getPassword() != null && userData.getPassword().equals(requestContent.getPassword())) {
                responseDTO.setUserId(userData.getUserId());
                responseDTO.setUsername(userData.getFullName());
                responseDTO.setLoginSuccess(true);
                responseDTO.setErrMsg(null);
            } else {
                responseDTO.setUserId(null);
                responseDTO.setLoginSuccess(false);
                responseDTO.setErrMsg("Password_Not_Match");
            }
        } else {
            responseDTO.setUserId(null);
            responseDTO.setLoginSuccess(false);
            responseDTO.setErrMsg("USER_NOT_EXISTS");
        }
        return responseDTO;
    }
}