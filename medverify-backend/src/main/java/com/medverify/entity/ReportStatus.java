package com.medverify.entity;

/**
 * Enum des statuts de signalement
 */
public enum ReportStatus {
    SUBMITTED, // Soumis
    UNDER_REVIEW, // En cours d'examen
    CONFIRMED, // Confirmé (contrefaçon avérée)
    REJECTED, // Rejeté (fausse alerte)
    CLOSED // Clos
}
