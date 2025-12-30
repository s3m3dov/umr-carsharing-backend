package com.singhv.auth_service.services.impl;

import com.singhv.auth_service.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.singhv.auth_service.repository.AuthUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
@Service
@RequiredArgsConstructor
public class CustomUserDetailServiceImpl implements UserDetailsService {
    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String emailID) throws UsernameNotFoundException {

        Users users;
        try {
            users = (Users) authUserRepository.findByEmailId(emailID);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return User.builder()
                .username(users.getEmailId())
                .password(users.getPassword())
                .roles("USER")
                .build();
    }
}
