package com.medverify.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitaire pour générer des hash BCrypt
 * À utiliser uniquement en développement
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String password = "Admin@123456";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash (strength 12): " + hash);
        System.out.println("\nSQL Command:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE email = 'admin@medverify.gw';");
        
        // Vérifier que le hash fonctionne
        boolean matches = encoder.matches(password, hash);
        System.out.println("\nVerification: " + (matches ? "✓ Hash is valid" : "✗ Hash is invalid"));
    }
}

