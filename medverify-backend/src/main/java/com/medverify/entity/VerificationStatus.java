package com.medverify.entity;

/**
 * Enum des statuts de vérification
 */
public enum VerificationStatus {
    AUTHENTIC, // Médicament authentique
    SUSPICIOUS, // Médicament suspect
    UNKNOWN // GTIN non trouvé dans la base
}

