package com.cimhub.authservice.service.impl;

import com.cimhub.authservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.cimhub.authservice.repository.AuthUserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailServiceImpl implements UserDetailsService {
    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String emailID) throws UsernameNotFoundException {

        User users;
        try {
            users = (User) authUserRepository.findByEmailId(emailID);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(users.getEmailId())
                .password(users.getPassword())
                .roles("USER")
                .build();
    }
}
