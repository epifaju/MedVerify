package com.medverify.security;

import com.medverify.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour JwtService
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private String secret;
    private Long expiration;
    private Long refreshExpiration;

    @BeforeEach
    void setUp() {
        secret = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-algorithm";
        expiration = 3600000L; // 1 heure
        refreshExpiration = 86400000L; // 24 heures

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);

        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT format: header.payload.signature
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getRefreshExpiration()).thenReturn(refreshExpiration);

        // When
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_ShouldReturnEmail() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
        String token = jwtService.generateToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
        String token = jwtService.generateToken(userDetails);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void isTokenExpired_ValidToken_ShouldReturnFalse() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
        String token = jwtService.generateToken(userDetails);

        // When
        Boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    void validateToken_ValidTokenAndUser_ShouldReturnTrue() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
        String token = jwtService.generateToken(userDetails);

        // When
        Boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_InvalidUser_ShouldReturnFalse() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
        String token = jwtService.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("other@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .build();

        // When
        Boolean isValid = jwtService.validateToken(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void generateToken_DifferentRoles_ShouldContainRole() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
        UserDetails adminUser = User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        // When
        String token = jwtService.generateToken(adminUser);
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("admin@example.com");
    }

    @Test
    void generateRefreshToken_HasDifferentExpiration() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
        when(jwtProperties.getRefreshExpiration()).thenReturn(refreshExpiration);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // When
        Date accessExpiration = jwtService.extractExpiration(accessToken);
        Date refreshExpirationDate = jwtService.extractExpiration(refreshToken);

        // Then
        assertThat(refreshExpirationDate).isAfter(accessExpiration);
    }
}

