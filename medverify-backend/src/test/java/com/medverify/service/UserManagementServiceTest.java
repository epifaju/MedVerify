package com.medverify.service;

import com.medverify.controller.AdminUserController.UserStatsResponse;
import com.medverify.dto.request.CreateUserRequest;
import com.medverify.dto.request.UpdateUserRequest;
import com.medverify.dto.response.MessageResponse;
import com.medverify.dto.response.UserResponse;
import com.medverify.entity.User;
import com.medverify.entity.UserRole;
import com.medverify.exception.DuplicateResourceException;
import com.medverify.exception.ResourceNotFoundException;
import com.medverify.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserManagementService
 */
@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserManagementService userManagementService;

    private User testUser;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone("123456789")
                .role(UserRole.PATIENT)
                .isVerified(true)
                .isActive(true)
                .failedLoginAttempts(0)
                .createdAt(Instant.now())
                .build();

        createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("newuser@example.com");
        createUserRequest.setPassword("TempPassword123!");
        createUserRequest.setFirstName("Jane");
        createUserRequest.setLastName("Smith");
        createUserRequest.setPhone("987654321");
        createUserRequest.setRole(UserRole.PATIENT);
        createUserRequest.setIsVerified(false);
        createUserRequest.setIsActive(true);

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("John Updated");
        updateUserRequest.setLastName("Doe Updated");
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> result = userManagementService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findAll(pageable);
    }

    @Test
    void searchUsers_NoFilters_ShouldReturnAllUsers() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> result = userManagementService.searchUsers(null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void getUserById_ValidId_ShouldReturnUser() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userManagementService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_InvalidId_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userManagementService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createUser_ValidRequest_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendWelcomeEmail(any(User.class), anyString());

        // When
        UserResponse result = userManagementService.createUser(createUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        assertThat(result.getFirstName()).isEqualTo("Jane");
        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail(any(User.class), eq("TempPassword123!"));
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> userManagementService.createUser(createUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ValidRequest_ShouldUpdateUser() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userManagementService.updateUser(userId, updateUserRequest);

        // Then
        assertThat(result).isNotNull();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFirstName()).isEqualTo("John Updated");
        assertThat(savedUser.getLastName()).isEqualTo("Doe Updated");
    }

    @Test
    void updateUser_InvalidId_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userManagementService.updateUser(userId, updateUserRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_DuplicateEmail_ShouldThrowException() {
        // Given
        UUID userId = testUser.getId();
        UpdateUserRequest requestWithEmail = new UpdateUserRequest();
        requestWithEmail.setEmail("duplicate@example.com");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> userManagementService.updateUser(userId, requestWithEmail))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void toggleUserActive_ActiveUser_ShouldDeactivate() {
        // Given
        UUID userId = testUser.getId();
        testUser.setIsActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        MessageResponse result = userManagementService.toggleUserActive(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("désactivé");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    void resetUserPassword_ValidUser_ShouldResetPassword() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(any(User.class), anyString());

        // When
        MessageResponse result = userManagementService.resetUserPassword(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("réinitialisé");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(0);
        assertThat(userCaptor.getValue().getLockedUntil()).isNull();
        verify(emailService).sendPasswordResetEmail(any(User.class), anyString());
    }

    @Test
    void deleteUser_ValidUser_ShouldSoftDelete() {
        // Given
        UUID userId = testUser.getId();
        testUser.setIsActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        MessageResponse result = userManagementService.deleteUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("désactivé");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    void getUserStats_ShouldReturnStats() {
        // Given
        List<User> allUsers = List.of(
                createUser(UserRole.PATIENT, true, true),
                createUser(UserRole.PHARMACIST, true, false),
                createUser(UserRole.AUTHORITY, false, true)
        );
        
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.findAll()).thenReturn(allUsers);
        when(userRepository.countNewUsers(any(Instant.class), any(Instant.class))).thenReturn(1L);

        // When
        UserStatsResponse stats = userManagementService.getUserStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.totalUsers()).isEqualTo(3);
        assertThat(stats.activeUsers()).isEqualTo(2);
        assertThat(stats.verifiedUsers()).isEqualTo(2);
    }

    @Test
    void createUser_WithNullOptionalFields_ShouldUseDefaults() {
        // Given
        CreateUserRequest requestWithoutOptional = new CreateUserRequest();
        requestWithoutOptional.setEmail("user@example.com");
        requestWithoutOptional.setPassword("Password123!");
        requestWithoutOptional.setFirstName("Jane");
        requestWithoutOptional.setLastName("Smith");
        requestWithoutOptional.setPhone("123456789");
        requestWithoutOptional.setRole(UserRole.PATIENT);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponse result = userManagementService.createUser(requestWithoutOptional);

        // Then
        assertThat(result).isNotNull();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getIsVerified()).isFalse();
        assertThat(savedUser.getIsActive()).isTrue();
    }

    private User createUser(UserRole role, boolean isActive, boolean isVerified) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(role.name().toLowerCase() + "@example.com")
                .password("encoded")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .isActive(isActive)
                .isVerified(isVerified)
                .failedLoginAttempts(0)
                .build();
    }
}

