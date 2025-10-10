package com.medverify.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entité ScanHistory - Historique des scans de vérification
 */
@Entity
@Table(name = "scan_history", indexes = {
        @Index(name = "idx_scan_user", columnList = "user_id"),
        @Index(name = "idx_scan_medication", columnList = "medication_id"),
        @Index(name = "idx_scan_gtin", columnList = "gtin"),
        @Index(name = "idx_scan_serial", columnList = "serial_number"),
        @Index(name = "idx_scan_status", columnList = "status"),
        @Index(name = "idx_scan_date", columnList = "scanned_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    @ToString.Exclude
    private Medication medication;

    @Column(nullable = false, length = 14)
    private String gtin;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Double confidence = 0.0;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();

    // Localisation PostGIS
    @Column(columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Type(JsonBinaryType.class)
    @Column(name = "device_info", columnDefinition = "jsonb")
    private DeviceInfo deviceInfo;

    @CreatedDate
    @Column(name = "scanned_at", nullable = false, updatable = false)
    private Instant scannedAt;

    /**
     * Classe pour les alertes (JSONB)
     */
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
     * Classe pour les informations de device (JSONB)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        private String platform; // Android, iOS
        private String osVersion;
        private String appVersion;
        private String deviceModel;
    }
}

