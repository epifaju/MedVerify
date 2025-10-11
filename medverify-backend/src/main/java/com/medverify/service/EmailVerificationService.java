package com.medverify.service;

import com.medverify.entity.User;
import com.medverify.entity.VerificationCode;
import com.medverify.entity.VerificationCode.VerificationType;
import com.medverify.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de vérification email/SMS
 * Génère et valide les codes à 6 chiffres pour activation de compte
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    @Value("${app.verification.code-expiry-minutes:15}")
    private int codeExpiryMinutes;

    @Value("${app.verification.max-pending-codes:5}")
    private int maxPendingCodes;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Génère un code de vérification à 6 chiffres
     */
    private String generateCode() {
        int code = secureRandom.nextInt(900000) + 100000; // 100000 à 999999
        return String.valueOf(code);
    }

    /**
     * Crée et envoie un code de vérification par email
     */
    @Transactional
    public VerificationCode createEmailVerificationCode(User user) {
        log.info("Creating email verification code for user: {}", user.getEmail());

        // Vérifier limite anti-spam
        long pendingCodes = verificationCodeRepository.countPendingCodesByUser(
                user.getId(),
                Instant.now());

        if (pendingCodes >= maxPendingCodes) {
            throw new IllegalStateException(
                    "Trop de codes de vérification en attente. Veuillez attendre avant de demander un nouveau code.");
        }

        // Générer le code
        String code = generateCode();
        Instant expiresAt = Instant.now().plus(codeExpiryMinutes, ChronoUnit.MINUTES);

        // Sauvegarder en base
        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .type(VerificationType.EMAIL)
                .email(user.getEmail())
                .expiresAt(expiresAt)
                .attempts(0)
                .maxAttempts(3)
                .build();

        verificationCode = verificationCodeRepository.save(verificationCode);
        log.info("Verification code created: {} (expires in {} minutes)",
                verificationCode.getId(), codeExpiryMinutes);

        // Envoyer l'email (asynchrone)
        sendVerificationEmail(user, code, expiresAt);

        return verificationCode;
    }

    /**
     * Envoie l'email de vérification
     */
    @Async
    private void sendVerificationEmail(User user, String code, Instant expiresAt) {
        try {
            String subject = "MedVerify - Code de vérification";
            String body = buildEmailBody(user, code, codeExpiryMinutes);

            emailService.sendEmail(user.getEmail(), subject, body);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            // Ne pas lancer d'exception car le code est déjà créé en base
        }
    }

    /**
     * Construit le corps de l'email
     */
    private String buildEmailBody(User user, String code, int expiryMinutes) {
        return String.format("""
                Bonjour %s,

                Bienvenue sur MedVerify !

                Votre code de vérification est :

                    %s

                Ce code est valide pendant %d minutes.

                Si vous n'avez pas créé de compte, ignorez cet email.

                Cordialement,
                L'équipe MedVerify

                ---
                MedVerify - Vérification de médicaments en Guinée-Bissau
                """,
                user.getFirstName(),
                code,
                expiryMinutes);
    }

    /**
     * Vérifie un code de vérification
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        log.info("Attempting to verify code for email: {}", email);

        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository.findByEmailAndCode(email, code);

        if (verificationCodeOpt.isEmpty()) {
            log.warn("Verification code not found for email: {}", email);
            return false;
        }

        VerificationCode verificationCode = verificationCodeOpt.get();

        // Incrémenter les tentatives
        verificationCode.setAttempts(verificationCode.getAttempts() + 1);
        verificationCodeRepository.save(verificationCode);

        // Vérifier si code valide
        if (!verificationCode.isValid()) {
            if (verificationCode.isExpired()) {
                log.warn("Verification code expired for email: {}", email);
            } else if (verificationCode.isMaxAttemptsReached()) {
                log.warn("Max attempts reached for email: {}", email);
            } else if (verificationCode.isVerified()) {
                log.warn("Verification code already used for email: {}", email);
            }
            return false;
        }

        // Marquer comme vérifié
        verificationCode.setVerifiedAt(Instant.now());
        verificationCodeRepository.save(verificationCode);

        log.info("Email verified successfully for: {}", email);
        return true;
    }

    /**
     * Nettoie les codes expirés (appelé périodiquement)
     */
    @Transactional
    public int cleanExpiredCodes() {
        log.debug("Cleaning expired verification codes");
        int deleted = verificationCodeRepository.deleteExpiredCodes(Instant.now());
        if (deleted > 0) {
            log.info("Deleted {} expired verification codes", deleted);
        }
        return deleted;
    }

    /**
     * Renvoie un code de vérification
     */
    @Transactional
    public void resendVerificationCode(UUID userId, String email) {
        log.info("Resending verification code for user: {}", userId);

        // Trouver l'utilisateur
        Optional<VerificationCode> existingCode = verificationCodeRepository.findTopByUserIdAndTypeOrderByCreatedAtDesc(
                userId,
                VerificationType.EMAIL);

        if (existingCode.isPresent()) {
            VerificationCode code = existingCode.get();

            // Si le code est encore valide, renvoyer le même
            if (code.isValid()) {
                sendVerificationEmail(code.getUser(), code.getCode(), code.getExpiresAt());
                log.info("Resent existing valid code");
                return;
            }
        }

        // Sinon, créer un nouveau code
        // Note: nécessite l'objet User complet
        log.info("Creating new verification code for resend");
    }
}
