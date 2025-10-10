package com.medverify.dto.response;

import com.medverify.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour la réponse de vérification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    private UUID verificationId;
    private VerificationStatus status;
    private Double confidence;
    private MedicationDetails medication;
    private List<Alert> alerts;
    private String verificationSource;
    private Instant verifiedAt;
    private String message; // Message optionnel pour expliquer le résultat

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationDetails {
        private UUID id;
        private String gtin;
        private String name;
        private String genericName;
        private String manufacturer;
        private String dosage;
        private String pharmaceuticalForm;
        private List<String> indications;
        private List<String> sideEffects;
        private List<String> contraindications;
        private PosologyDetails posology;
        private Boolean isEssential;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PosologyDetails {
        private String adult;
        private String child;
        private Integer maxDailyDose;
        private String unit;
        private String frequency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String severity; // HIGH, MEDIUM, LOW
        private String code;
        private String message;
    }

    /**
     * Factory pour médicament inconnu
     */
    public static VerificationResponse unknown() {
        return VerificationResponse.builder()
                .status(VerificationStatus.UNKNOWN)
                .confidence(0.0)
                .verificationSource("DATABASE")
                .verifiedAt(Instant.now())
                .build();
    }
}
