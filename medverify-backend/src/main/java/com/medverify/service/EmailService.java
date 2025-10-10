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
}

