package com.medverify.dto.response;

import com.medverify.entity.ScanHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour la réponse des scans suspects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousScanResponse {
    private UUID id;
    private String gtin;
    private String serialNumber;
    private String batchNumber;
    private Double confidence;
    private Instant scannedAt;
    private String medicationName;
    private String medicationId;
    private String userName;
    private String userEmail;
    private List<AlertInfo> alerts;
    private LocationInfo location;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertInfo {
        private String severity;
        private String code;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
    }

    /**
     * Convertit une ScanHistory en SuspiciousScanResponse
     */
    public static SuspiciousScanResponse fromScanHistory(ScanHistory scanHistory) {
        SuspiciousScanResponseBuilder builder = SuspiciousScanResponse.builder()
                .id(scanHistory.getId())
                .gtin(scanHistory.getGtin())
                .serialNumber(scanHistory.getSerialNumber())
                .batchNumber(scanHistory.getBatchNumber())
                .confidence(scanHistory.getConfidence())
                .scannedAt(scanHistory.getScannedAt())
                .medicationName(scanHistory.getMedication() != null ? scanHistory.getMedication().getName() : null)
                .medicationId(scanHistory.getMedication() != null ? scanHistory.getMedication().getId().toString() : null)
                .userName(scanHistory.getUser() != null 
                        ? scanHistory.getUser().getFirstName() + " " + scanHistory.getUser().getLastName() 
                        : null)
                .userEmail(scanHistory.getUser() != null ? scanHistory.getUser().getEmail() : null);

        // Convertir les alertes
        if (scanHistory.getAlerts() != null && !scanHistory.getAlerts().isEmpty()) {
            builder.alerts(scanHistory.getAlerts().stream()
                    .map(alert -> AlertInfo.builder()
                            .severity(alert.getSeverity())
                            .code(alert.getCode())
                            .message(alert.getMessage())
                            .build())
                    .toList());
        }

        // Convertir la localisation
        if (scanHistory.getLocation() != null) {
            builder.location(LocationInfo.builder()
                    .latitude(scanHistory.getLocation().getY())
                    .longitude(scanHistory.getLocation().getX())
                    .build());
        }

        return builder.build();
    }
}

