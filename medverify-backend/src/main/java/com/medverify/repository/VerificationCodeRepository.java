package com.medverify.repository;

import com.medverify.entity.VerificationCode;
import com.medverify.entity.VerificationCode.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité VerificationCode
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    /**
     * Trouve le code de vérification le plus récent pour un utilisateur et un type
     */
    Optional<VerificationCode> findTopByUserIdAndTypeOrderByCreatedAtDesc(
            UUID userId,
            VerificationType type);

    /**
     * Trouve un code de vérification par email et code
     */
    @Query("SELECT vc FROM VerificationCode vc " +
            "WHERE vc.email = :email " +
            "AND vc.code = :code " +
            "AND vc.type = 'EMAIL' " +
            "AND vc.verifiedAt IS NULL " +
            "ORDER BY vc.createdAt DESC " +
            "LIMIT 1")
    Optional<VerificationCode> findByEmailAndCode(
            @Param("email") String email,
            @Param("code") String code);

    /**
     * Trouve un code de vérification par téléphone et code
     */
    @Query("SELECT vc FROM VerificationCode vc " +
            "WHERE vc.phone = :phone " +
            "AND vc.code = :code " +
            "AND vc.type = 'SMS' " +
            "AND vc.verifiedAt IS NULL " +
            "ORDER BY vc.createdAt DESC " +
            "LIMIT 1")
    Optional<VerificationCode> findByPhoneAndCode(
            @Param("phone") String phone,
            @Param("code") String code);

    /**
     * Supprime les codes expirés
     */
    @Modifying
    @Query("DELETE FROM VerificationCode vc WHERE vc.expiresAt < :now")
    int deleteExpiredCodes(@Param("now") Instant now);

    /**
     * Compte les codes non vérifiés pour un utilisateur (limite anti-spam)
     */
    @Query("SELECT COUNT(vc) FROM VerificationCode vc " +
            "WHERE vc.user.id = :userId " +
            "AND vc.verifiedAt IS NULL " +
            "AND vc.expiresAt > :now")
    long countPendingCodesByUser(
            @Param("userId") UUID userId,
            @Param("now") Instant now);
}



