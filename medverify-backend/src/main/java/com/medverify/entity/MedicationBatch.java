package com.medverify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité MedicationBatch - Lots de médicaments
 */
@Entity
@Table(name = "medication_batches", indexes = {
        @Index(name = "idx_batch_number", columnList = "batch_number"),
        @Index(name = "idx_batch_medication", columnList = "medication_id"),
        @Index(name = "idx_batch_recalled", columnList = "is_recalled")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    @ToString.Exclude
    private Medication medication;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "is_recalled", nullable = false)
    @Builder.Default
    private Boolean isRecalled = false;

    @Column(name = "recall_date")
    private LocalDate recallDate;

    @Column(name = "recall_reason", length = 1000)
    private String recallReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

