package com.cimhub.authservice.service;

import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String emailID) throws UsernameNotFoundException {

        User users = authUserRepository.findByEmailId(emailID);
        if (users == null) {
            throw new UsernameNotFoundException("User not found with email: " + emailID);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(users.getEmailId())
                .password(users.getPassword())
                .roles("USER")
                .build();
    }
}
