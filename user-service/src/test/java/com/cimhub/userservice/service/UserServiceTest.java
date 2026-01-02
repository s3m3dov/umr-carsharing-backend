package com.cimhub.userservice.service;

import com.cimhub.common.exception.BusinessException;
import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private CreateProfileRequest createProfileRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        createProfileRequest = CreateProfileRequest.builder()
                .userId("test@example.com")
                .emailId("test@example.com")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .age(25)
                .dob("1998-01-01")
                .empId("EMP001")
                .organisationName("Test Org")
                .profilePicUrl("https://example.com/pic.jpg")
                .build();

        testUser = new User();
        testUser.setUserId("test@example.com");
        testUser.setEmailId("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhoneNumber("+1234567890");
        testUser.setAge(25);
        testUser.setDob("1998-01-01");
        testUser.setEmpId("EMP001");
        testUser.setOrganisationName("Test Org");
        testUser.setProfilePicUrl("https://example.com/pic.jpg");
        testUser.setCreatedDate(new Date());
    }

    @Test
    void createProfile_whenValidRequest_thenSuccessfullyCreatesProfile() {
        // Given
        when(userRepository.findByUserId(anyString())).thenReturn(null);

        // When
        userService.createProfile(createProfileRequest);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUserId()).isEqualTo("test@example.com");
        assertThat(savedUser.getEmailId()).isEqualTo("test@example.com");
        assertThat(savedUser.getFullName()).isEqualTo("Test User");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(savedUser.getAge()).isEqualTo(25);
        assertThat(savedUser.getCreatedBy()).isEqualTo("test@example.com");
        assertThat(savedUser.getCreatedDate()).isNotNull();
    }

    @Test
    void createProfile_whenUserAlreadyExists_thenThrowsBusinessException() {
        // Given
        when(userRepository.findByUserId(anyString())).thenReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> userService.createProfile(createProfileRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User profile")
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createProfile_whenCalled_thenSetsAllFieldsCorrectly() {
        // Given
        when(userRepository.findByUserId(anyString())).thenReturn(null);

        // When
        userService.createProfile(createProfileRequest);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmpId()).isEqualTo("EMP001");
        assertThat(savedUser.getOrganisationName()).isEqualTo("Test Org");
        assertThat(savedUser.getProfilePicUrl()).isEqualTo("https://example.com/pic.jpg");
        assertThat(savedUser.getDob()).isEqualTo("1998-01-01");
    }

    @Test
    void getUserInfo_whenUserExists_thenReturnsUserResponse() {
        // Given
        when(userRepository.findByUserId(anyString())).thenReturn(testUser);

        // When
        UserResponse response = userService.getUserInfo("test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.fullName()).isEqualTo("Test User");
        assertThat(response.emailId()).isEqualTo("test@example.com");
        assertThat(response.userId()).isEqualTo("test@example.com");
        assertThat(response.phoneNumber()).isEqualTo("+1234567890");
        assertThat(response.age()).isEqualTo(25);
        assertThat(response.dob()).isEqualTo("1998-01-01");
        assertThat(response.empId()).isEqualTo("EMP001");
        assertThat(response.organisationName()).isEqualTo("Test Org");
        assertThat(response.profilePicUrl()).isEqualTo("https://example.com/pic.jpg");

        verify(userRepository).findByUserId("test@example.com");
    }

    @Test
    void getUserInfo_whenUserDoesNotExist_thenThrowsBusinessException() {
        // Given
        when(userRepository.findByUserId(anyString())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userService.getUserInfo("nonexistent@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("nonexistent@example.com");

        verify(userRepository).findByUserId("nonexistent@example.com");
    }

    @Test
    void getUserInfo_whenCalledMultipleTimes_thenQueriesRepositoryEachTime() {
        // Given
        when(userRepository.findByUserId(anyString())).thenReturn(testUser);

        // When
        userService.getUserInfo("test@example.com");
        userService.getUserInfo("test@example.com");

        // Then
        verify(userRepository, times(2)).findByUserId("test@example.com");
    }
}
