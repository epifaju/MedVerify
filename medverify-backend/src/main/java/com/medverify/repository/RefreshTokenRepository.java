package com.medverify.repository;

import com.medverify.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité RefreshToken
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Trouve un refresh token par sa valeur
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Supprime tous les tokens d'un utilisateur
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Révoque tous les tokens d'un utilisateur
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    void revokeAllByUserId(@Param("userId") UUID userId);
}

