package com.medverify.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité Report - Signalements de médicaments suspects
 */
@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_user", columnList = "user_id"),
        @Index(name = "idx_report_medication", columnList = "medication_id"),
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_reference", columnList = "reference_number", unique = true),
        @Index(name = "idx_report_date", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    @ToString.Exclude
    private Medication medication;

    @Column(length = 14)
    private String gtin;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    @Column(length = 2000)
    private String description;

    @Type(JsonBinaryType.class)
    @Column(name = "purchase_location", columnDefinition = "jsonb")
    private PurchaseLocation purchaseLocation;

    @ElementCollection
    @CollectionTable(name = "report_photos", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "photo_url", length = 500)
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean anonymous = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.SUBMITTED;

    @Column(name = "reference_number", unique = true, length = 50)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @ToString.Exclude
    private User reviewer;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Classe pour les informations de lieu d'achat (JSONB)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseLocation {
        private String name;
        private String address;
        private String city;
        private String region;
        private Double latitude;
        private Double longitude;
    }
}
