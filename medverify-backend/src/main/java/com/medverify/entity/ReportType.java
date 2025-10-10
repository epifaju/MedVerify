package com.medverify.entity;

/**
 * Enum des types de signalement
 */
public enum ReportType {
    COUNTERFEIT, // Médicament contrefait
    QUALITY_ISSUE, // Problème de qualité
    EXPIRED, // Médicament périmé
    PACKAGING_DEFECT, // Défaut d'emballage
    ADVERSE_REACTION, // Réaction indésirable
    OTHER // Autre
}
