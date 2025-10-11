package com.medverify.service;

import com.medverify.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    /**
     * Vérifie si l'email est correctement configuré
     */
    private boolean isEmailConfigured() {
        return fromEmail != null && !fromEmail.trim().isEmpty()
                && emailPassword != null && !emailPassword.trim().isEmpty();
    }

    /**
     * Envoie un email de vérification
     */
    @Async
    public void sendVerificationEmail(User user) {
        // Vérifier si l'email est configuré
        if (!isEmailConfigured()) {
            log.info("Email service not configured. Skipping verification email for: {}", user.getEmail());
            return;
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("MedVerify - Verify Your Account");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "Thank you for registering with MedVerify.\n\n" +
                            "Please verify your account to start using the app.\n\n" +
                            "Best regards,\n" +
                            "The MedVerify Team",
                    user.getFullName()));

            mailSender.send(message);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send verification email to: {} - Email service may not be configured", user.getEmail());
            log.debug("Email error details:", e);
        }
    }

    /**
     * Envoie un email de notification aux autorités
     */
    @Async
    public void sendSuspiciousMedicationAlert(String medicationName, String gtin, String location) {
        // Vérifier si l'email est configuré
        if (!isEmailConfigured()) {
            log.info("Email service not configured. Skipping suspicious medication alert for GTIN: {}", gtin);
            return;
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail); // À remplacer par les emails des autorités
            message.setSubject("MedVerify - Suspicious Medication Alert");
            message.setText(String.format(
                    "ALERT: Suspicious medication detected!\n\n" +
                            "Medication: %s\n" +
                            "GTIN: %s\n" +
                            "Location: %s\n\n" +
                            "Please investigate immediately.",
                    medicationName, gtin, location));

            mailSender.send(message);
            log.info("Suspicious medication alert sent");
        } catch (Exception e) {
            log.warn("Failed to send suspicious medication alert - Email service may not be configured");
            log.debug("Email error details:", e);
        }
    }

    /**
     * Envoie un email de bienvenue avec les identifiants
     */
    @Async
    public void sendWelcomeEmail(User user, String temporaryPassword) {
        if (!isEmailConfigured()) {
            log.info("Email service not configured. Skipping welcome email for: {}", user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("MedVerify - Bienvenue !");
            message.setText(String.format(
                    "Bonjour %s %s,\n\n" +
                            "Votre compte MedVerify a été créé avec succès.\n\n" +
                            "Identifiants de connexion :\n" +
                            "Email : %s\n" +
                            "Mot de passe temporaire : %s\n\n" +
                            "Pour des raisons de sécurité, nous vous recommandons de changer votre mot de passe lors de votre première connexion.\n\n"
                            +
                            "Cordialement,\n" +
                            "L'équipe MedVerify",
                    user.getFirstName(), user.getLastName(), user.getEmail(), temporaryPassword));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send welcome email to: {}", user.getEmail());
            log.debug("Email error details:", e);
        }
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    @Async
    public void sendPasswordResetEmail(User user, String temporaryPassword) {
        if (!isEmailConfigured()) {
            log.info("Email service not configured. Skipping password reset email for: {}", user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("MedVerify - Réinitialisation de mot de passe");
            message.setText(String.format(
                    "Bonjour %s %s,\n\n" +
                            "Votre mot de passe a été réinitialisé par un administrateur.\n\n" +
                            "Nouveau mot de passe temporaire : %s\n\n" +
                            "Pour des raisons de sécurité, veuillez changer ce mot de passe lors de votre prochaine connexion.\n\n"
                            +
                            "Si vous n'avez pas demandé cette réinitialisation, contactez immédiatement un administrateur.\n\n"
                            +
                            "Cordialement,\n" +
                            "L'équipe MedVerify",
                    user.getFirstName(), user.getLastName(), temporaryPassword));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send password reset email to: {}", user.getEmail());
            log.debug("Email error details:", e);
        }
    }
}
