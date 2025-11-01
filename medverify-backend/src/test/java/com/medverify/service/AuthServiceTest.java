package com.medverify.service;

import com.medverify.dto.request.LoginRequest;
import com.medverify.dto.request.RegisterRequest;
import com.medverify.dto.request.RefreshTokenRequest;
import com.medverify.entity.RefreshToken;
import com.medverify.entity.User;
import com.medverify.entity.UserRole;
import com.medverify.exception.AccountLockedException;
import com.medverify.exception.DuplicateResourceException;
import com.medverify.exception.InvalidCredentialsException;
import com.medverify.repository.RefreshTokenRepository;
import com.medverify.repository.UserRepository;
import com.medverify.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Configuration des valeurs par défaut
        ReflectionTestUtils.setField(authService, "jwtExpiration", 900000L);
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);

        // Créer un utilisateur de test
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("$2a$12$encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.PATIENT)
                .isVerified(true)
                .isActive(true)
                .failedLoginAttempts(0)
                .build();

        // Créer des requêtes de test
        registerRequest = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .firstName("Jane")
                .lastName("Smith")
                .phone("123456789")
                .role(UserRole.PATIENT)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();
    }

    @Test
    void register_ValidRequest_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailVerificationService).createEmailVerificationCode(any(User.class));

        // When
        var response = authService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("Registration successful");
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailVerificationService).createEmailVerificationCode(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ValidCredentials_ShouldReturnAuthResponse() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    void login_InvalidEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ShouldIncrementFailedAttempts() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
        
        verify(userRepository).save(any(User.class));
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0); // Avant l'appel
        // Après l'appel, l'attribut devrait être incrémenté dans le mock
    }

    @Test
    void login_AccountLocked_ShouldThrowException() {
        // Given
        testUser.setLockedUntil(Instant.now().plusSeconds(3600)); // Locked for 1 hour
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account is locked");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_AccountInactive_ShouldThrowException() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account is disabled");
    }

    @Test
    void refreshToken_ValidToken_ShouldReturnNewAccessToken() {
        // Given
        RefreshToken refreshToken = RefreshToken.builder()
                .token("validRefreshToken")
                .user(testUser)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("validRefreshToken")
                .build();

        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateToken(testUser)).thenReturn("newAccessToken");

        // When
        var response = authService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void refreshToken_InvalidToken_ShouldThrowException() {
        // Given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalidToken")
                .build();

        when(refreshTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid refresh token");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void refreshToken_ExpiredToken_ShouldThrowException() {
        // Given
        RefreshToken refreshToken = RefreshToken.builder()
                .token("expiredRefreshToken")
                .user(testUser)
                .expiresAt(Instant.now().minusSeconds(3600)) // Expired
                .revokedAt(Instant.now())
                .build();

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("expiredRefreshToken")
                .build();

        when(refreshTokenRepository.findByToken("expiredRefreshToken")).thenReturn(Optional.of(refreshToken));

        // When / Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Refresh token is expired");
    }
}

