package com.medverify.service;

import com.medverify.dto.request.LoginRequest;
import com.medverify.dto.request.RefreshTokenRequest;
import com.medverify.dto.request.RegisterRequest;
import com.medverify.dto.response.AuthResponse;
import com.medverify.dto.response.MessageResponse;
import com.medverify.entity.RefreshToken;
import com.medverify.entity.User;
import com.medverify.exception.AccountLockedException;
import com.medverify.exception.DuplicateResourceException;
import com.medverify.exception.InvalidCredentialsException;
import com.medverify.exception.ResourceNotFoundException;
import com.medverify.repository.RefreshTokenRepository;
import com.medverify.repository.UserRepository;
import com.medverify.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Service d'authentification
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofHours(1);

    /**
     * Enregistre un nouvel utilisateur
     */
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isVerified(false)
                .isActive(true)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Envoyer email de vérification (async)
        emailService.sendVerificationEmail(user);

        return MessageResponse.of("Registration successful. Please check your email to verify your account.");
    }

    /**
     * Authentifie un utilisateur
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Vérifier si le compte est bloqué
        if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts. Try again later.");
        }

        // Réinitialiser le verrouillage si la période est expirée
        if (user.getLockedUntil() != null && Instant.now().isAfter(user.getLockedUntil())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Vérifier si le compte est actif
        if (!user.getIsActive()) {
            throw new AccountLockedException("Account is disabled");
        }

        // Réinitialiser les tentatives échouées
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Générer les tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Sauvegarder le refresh token
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .isVerified(user.getIsVerified())
                        .build())
                .build();
    }

    /**
     * Rafraîchit l'access token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Attempting to refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        // Vérifier si le token est valide
        if (!refreshToken.isValid()) {
            throw new InvalidCredentialsException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        // Générer un nouveau access token
        String newAccessToken = jwtService.generateToken(user);

        log.info("Token refreshed successfully for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .isVerified(user.getIsVerified())
                        .build())
                .build();
    }

    /**
     * Gère les tentatives de connexion échouées
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
            log.warn("Account locked for user: {} after {} failed attempts", user.getEmail(), attempts);
        }

        userRepository.save(user);
        log.warn("Failed login attempt #{} for user: {}", attempts, user.getEmail());
    }
}

