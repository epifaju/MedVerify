package com.medverify.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties pour JWT avec validation au démarrage
 * Fait échouer le démarrage si le secret JWT est vide ou trop faible
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
@Slf4j
public class JwtProperties {

    @NotBlank(message = "JWT secret must not be blank. Set JWT_SECRET environment variable or configure in application.yml")
    @Size(min = 32, message = "JWT secret must be at least 32 characters long for security")
    private String secret;

    private Long expiration = 900000L; // 15 minutes default

    private Long refreshExpiration = 604800000L; // 7 days default

    /**
     * Valide la configuration au démarrage
     * Génère un message d'erreur clair si le secret est manquant ou trop court
     */
    @PostConstruct
    public void validate() {
        if (secret == null || secret.trim().isEmpty()) {
            String errorMessage = """
                ⚠️ ERREUR CRITIQUE DE SÉCURITÉ ⚠️
                
                Le secret JWT n'est pas configuré !
                
                Pour corriger :
                1. Générer un secret fort (recommandé) :
                   openssl rand -base64 32
                
                2. Configurer l'une des options suivantes :
                   - Variable d'environnement : export JWT_SECRET="votre-secret-genere"
                   - Fichier application-local.yml : jwt.secret=votre-secret-genere
                   - Paramètre JVM : -Djwt.secret=votre-secret-genere
                
                3. Redémarrer l'application
                
                ⚠️ L'application ne peut pas démarrer sans un secret JWT valide !
                """;
            log.error(errorMessage);
            throw new IllegalStateException(
                "JWT secret is required but not configured. " +
                "Set JWT_SECRET environment variable or configure jwt.secret in application.yml. " +
                "Generate a secure secret with: openssl rand -base64 32"
            );
        }

        if (secret.length() < 32) {
            log.warn("JWT secret is too short ({} characters). Minimum recommended: 32 characters.", secret.length());
            log.warn("Generate a secure secret with: openssl rand -base64 32");
        }

        log.info("JWT configuration validated successfully. Secret length: {} characters", secret.length());
    }
}

