package com.cimhub.authservice.service;

import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("test@example.com");
        testUser.setEmailId("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setCreatedDate(new Date());
    }

    @Test
    void loadUserByUsername_whenUserExists_thenReturnsUserDetails() {
        // Given
        when(authUserRepository.findByEmailId(anyString())).thenReturn(testUser);

        // When
        UserDetails userDetails = customUserDetailService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        verify(authUserRepository).findByEmailId("test@example.com");
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_thenThrowsUsernameNotFoundException() {
        // Given
        when(authUserRepository.findByEmailId(anyString())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> customUserDetailService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: nonexistent@example.com");

        verify(authUserRepository).findByEmailId("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_whenCalled_thenQueriesRepositoryWithCorrectEmail() {
        // Given
        String email = "specific@example.com";
        testUser.setEmailId(email);
        when(authUserRepository.findByEmailId(email)).thenReturn(testUser);

        // When
        customUserDetailService.loadUserByUsername(email);

        // Then
        verify(authUserRepository).findByEmailId(email);
    }
}
