package com.medverify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity pour les codes de vérification email/SMS
 * Utilisé pour l'activation des comptes utilisateurs
 */
@Entity
@Table(name = "verification_codes", indexes = {
        @Index(name = "idx_verification_user_type", columnList = "user_id, type"),
        @Index(name = "idx_verification_expires", columnList = "expires_at"),
        @Index(name = "idx_verification_code", columnList = "code")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(nullable = false, length = 6)
    private String code; // Code à 6 chiffres

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationType type; // EMAIL ou SMS

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Type de vérification
     */
    public enum VerificationType {
        EMAIL,
        SMS
    }

    /**
     * Vérifier si le code est encore valide (non expiré)
     */
    public boolean isValid() {
        return verifiedAt == null
                && expiresAt.isAfter(Instant.now())
                && attempts < maxAttempts;
    }

    /**
     * Vérifier si le code est déjà utilisé
     */
    public boolean isVerified() {
        return verifiedAt != null;
    }

    /**
     * Vérifier si le code est expiré
     */
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    /**
     * Vérifier si le nombre max de tentatives est atteint
     */
    public boolean isMaxAttemptsReached() {
        return attempts >= maxAttempts;
    }
}


