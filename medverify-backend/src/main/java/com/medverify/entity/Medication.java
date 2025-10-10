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
 * Entité Medication - Médicaments référencés
 */
@Entity
@Table(name = "medications", indexes = {
        @Index(name = "idx_medication_gtin", columnList = "gtin", unique = true),
        @Index(name = "idx_medication_name", columnList = "name"),
        @Index(name = "idx_medication_essential", columnList = "is_essential"),
        @Index(name = "idx_medication_active", columnList = "is_active")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 14)
    private String gtin; // Global Trade Item Number

    @Column(name = "cip13", length = 13)
    private String cip13; // Code Identifiant Présentation (13 chiffres)

    @Column(name = "cis")
    private Long cis; // Code Identifiant Spécialité (BDPM)

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "generic_name", length = 255)
    private String genericName;

    @Column(length = 255)
    private String manufacturer;

    @Column(name = "manufacturer_country", length = 100)
    private String manufacturerCountry;

    @Column(length = 100)
    private String dosage;

    @Column(name = "pharmaceutical_form", length = 100)
    private String pharmaceuticalForm; // Comprimé, Sirop, Injectable, etc.

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "atc_code", length = 20)
    private String atcCode; // Anatomical Therapeutic Chemical Classification

    // JSONB field pour posologie
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private PosologyInfo posology;

    @ElementCollection
    @CollectionTable(name = "medication_indications", joinColumns = @JoinColumn(name = "medication_id"))
    @Column(name = "indication", length = 500)
    @Builder.Default
    private List<String> indications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "medication_side_effects", joinColumns = @JoinColumn(name = "medication_id"))
    @Column(name = "side_effect", length = 500)
    @Builder.Default
    private List<String> sideEffects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "medication_contraindications", joinColumns = @JoinColumn(name = "medication_id"))
    @Column(name = "contraindication", length = 500)
    @Builder.Default
    private List<String> contraindications = new ArrayList<>();

    @Column(name = "is_essential", nullable = false)
    @Builder.Default
    private Boolean isEssential = false; // Médicament essentiel OMS

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "requires_prescription", nullable = false)
    @Builder.Default
    private Boolean requiresPrescription = false;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<MedicationBatch> batches = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Classe pour informations de posologie (JSONB)
     * Note: Ne PAS utiliser @Embeddable car c'est stocké comme JSONB
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PosologyInfo {
        private String adult;
        private String child;
        private String elderly;
        private Integer maxDailyDose;
        private String unit;
        private String frequency;
        private Integer duration;
        private String specialInstructions;
    }
}
