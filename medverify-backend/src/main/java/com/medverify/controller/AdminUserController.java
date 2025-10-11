package com.medverify.controller;

import com.medverify.dto.request.CreateUserRequest;
import com.medverify.dto.request.UpdateUserRequest;
import com.medverify.dto.response.MessageResponse;
import com.medverify.dto.response.UserResponse;
import com.medverify.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Contrôleur Admin pour la gestion des utilisateurs
 * Accessible uniquement aux ADMIN
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Users", description = "Gestion des utilisateurs (ADMIN uniquement)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserManagementService userManagementService;

    /**
     * Liste tous les utilisateurs (paginé)
     */
    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs", description = "Retourne la liste paginée de tous les utilisateurs")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin listing all users, page: {}", pageable.getPageNumber());
        Page<UserResponse> users = userManagementService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Recherche des utilisateurs par email, nom ou rôle
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des utilisateurs", description = "Recherche par email, nom, prénom ou rôle")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin searching users: query={}, role={}", query, role);
        Page<UserResponse> users = userManagementService.searchUsers(query, role, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Obtenir les détails d'un utilisateur
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Détails d'un utilisateur", description = "Retourne les informations complètes d'un utilisateur")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("Admin getting user details: {}", userId);
        UserResponse user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Créer un nouvel utilisateur (par un admin)
     */
    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Crée un nouvel utilisateur (admin peut choisir le rôle)")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Admin creating user: {}", request.getEmail());
        UserResponse user = userManagementService.createUser(request);
        return ResponseEntity.ok(user);
    }

    /**
     * Mettre à jour un utilisateur
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Modifier un utilisateur", description = "Met à jour les informations d'un utilisateur")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Admin updating user: {}", userId);
        UserResponse user = userManagementService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    @PatchMapping("/{userId}/toggle-active")
    @Operation(summary = "Activer/Désactiver un utilisateur", description = "Change le statut actif d'un utilisateur")
    public ResponseEntity<MessageResponse> toggleUserActive(@PathVariable UUID userId) {
        log.info("Admin toggling user active status: {}", userId);
        MessageResponse response = userManagementService.toggleUserActive(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe", description = "Génère un nouveau mot de passe temporaire et l'envoie par email")
    public ResponseEntity<MessageResponse> resetUserPassword(@PathVariable UUID userId) {
        log.info("Admin resetting password for user: {}", userId);
        MessageResponse response = userManagementService.resetUserPassword(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un utilisateur (soft delete - désactivation)
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Supprimer un utilisateur", description = "Désactive un utilisateur (soft delete)")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable UUID userId) {
        log.info("Admin deleting user: {}", userId);
        MessageResponse response = userManagementService.deleteUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Statistiques des utilisateurs
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques utilisateurs", description = "Retourne les statistiques globales des utilisateurs")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        log.info("Admin getting user statistics");
        UserStatsResponse stats = userManagementService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * DTO pour les statistiques
     */
    public record UserStatsResponse(
            long totalUsers,
            long activeUsers,
            long verifiedUsers,
            long patientCount,
            long pharmacistCount,
            long authorityCount,
            long adminCount,
            long newUsersLast30Days) {
    }
}
