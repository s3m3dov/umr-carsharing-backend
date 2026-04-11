package com.cimhub.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.common.enums.UserType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    private CustomUserDetailService customUserDetailService;

    @BeforeEach
    void setUp() {
        customUserDetailService = new CustomUserDetailService(authUserRepository);
    }

    @Test
    void loadUserByUsername_shouldNormalizeEmailAndBuildUserDetails() {
        User user = new User();
        user.setEmail("driver@example.com");
        user.setPassword("hashed-password");
        user.setUserType(UserType.DRIVER);

        when(authUserRepository.findByEmail("driver@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailService.loadUserByUsername("Driver@Example.COM ");

        assertEquals("driver@example.com", userDetails.getUsername());
        assertEquals("hashed-password", userDetails.getPassword());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
        verify(authUserRepository).findByEmail("driver@example.com");
    }

    @Test
    void loadUserByUsername_whenMissingUser_shouldThrow() {
        when(authUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailService.loadUserByUsername("missing@example.com"));
    }
}
