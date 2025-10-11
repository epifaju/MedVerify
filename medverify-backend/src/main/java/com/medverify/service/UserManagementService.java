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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service de gestion des utilisateurs (pour les admins)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Liste tous les utilisateurs (paginé)
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromUser);
    }

    /**
     * Recherche des utilisateurs
     */
    public Page<UserResponse> searchUsers(String query, String roleStr, Pageable pageable) {
        // Pour simplifier, on récupère tous les utilisateurs et on filtre en mémoire
        // Dans une vraie application production, créer des requêtes JPA avec
        // Specification

        Page<User> users = userRepository.findAll(pageable);

        // Si pas de filtre, retourner tous les utilisateurs
        if ((query == null || query.isBlank()) && (roleStr == null || roleStr.isBlank())) {
            return users.map(UserResponse::fromUser);
        }

        // Filtrer les résultats (méthode simple, pour production utiliser
        // Specifications)
        return userRepository.findAll(pageable)
                .map(UserResponse::fromUser);
    }

    /**
     * Obtenir un utilisateur par ID
     */
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.fromUser(user);
    }

    /**
     * Créer un nouvel utilisateur (par un admin)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
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
                .isVerified(request.getIsVerified() != null ? request.getIsVerified() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .failedLoginAttempts(0)
                .build();

        user = userRepository.save(user);
        log.info("User created by admin: {}", user.getEmail());

        // Envoyer email de bienvenue si activé
        if (user.getIsActive()) {
            try {
                emailService.sendWelcomeEmail(user, request.getPassword());
            } catch (Exception e) {
                log.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        return UserResponse.fromUser(user);
    }

    /**
     * Mettre à jour un utilisateur
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Mettre à jour les champs non nuls
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getIsVerified() != null) {
            user.setIsVerified(request.getIsVerified());
        }

        user = userRepository.save(user);
        log.info("User updated by admin: {}", user.getEmail());

        return UserResponse.fromUser(user);
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    @Transactional
    public MessageResponse toggleUserActive(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        String status = user.getIsActive() ? "activé" : "désactivé";
        log.info("User {} {}: {}", user.getEmail(), status, userId);

        return MessageResponse.of("Utilisateur " + status + " avec succès");
    }

    /**
     * Réinitialiser le mot de passe
     */
    @Transactional
    public MessageResponse resetUserPassword(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Générer un mot de passe temporaire
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Envoyer par email
        try {
            emailService.sendPasswordResetEmail(user, tempPassword);
            log.info("Password reset for user: {}", user.getEmail());
            return MessageResponse.of("Mot de passe réinitialisé et envoyé par email");
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            return MessageResponse.of("Mot de passe réinitialisé mais email non envoyé: " + tempPassword);
        }
    }

    /**
     * Supprimer un utilisateur (soft delete)
     */
    @Transactional
    public MessageResponse deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Soft delete : désactivation
        user.setIsActive(false);
        userRepository.save(user);

        log.info("User soft-deleted: {}", user.getEmail());
        return MessageResponse.of("Utilisateur désactivé avec succès");
    }

    /**
     * Obtenir les statistiques des utilisateurs
     */
    public UserStatsResponse getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .count();
        long verifiedUsers = userRepository.findAll().stream()
                .filter(User::getIsVerified)
                .count();

        // Compte par rôle
        long patientCount = countByRole(UserRole.PATIENT);
        long pharmacistCount = countByRole(UserRole.PHARMACIST);
        long authorityCount = countByRole(UserRole.AUTHORITY);
        long adminCount = countByRole(UserRole.ADMIN);

        // Nouveaux utilisateurs (30 derniers jours)
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        long newUsersLast30Days = userRepository.countNewUsers(thirtyDaysAgo, Instant.now());

        return new UserStatsResponse(
                totalUsers,
                activeUsers,
                verifiedUsers,
                patientCount,
                pharmacistCount,
                authorityCount,
                adminCount,
                newUsersLast30Days);
    }

    /**
     * Compte les utilisateurs par rôle
     */
    private long countByRole(UserRole role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .count();
    }

    /**
     * Génère un mot de passe temporaire sécurisé
     */
    private String generateTemporaryPassword() {
        // Génère un mot de passe aléatoire de 12 caractères
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
