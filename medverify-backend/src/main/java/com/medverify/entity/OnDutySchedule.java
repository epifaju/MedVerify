package com.medverify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Représente un planning de garde pour une pharmacie
 */
@Entity
@Table(name = "on_duty_schedules", indexes = {
        @Index(name = "idx_on_duty_date_range", columnList = "start_date, end_date"),
        @Index(name = "idx_on_duty_region", columnList = "region"),
        @Index(name = "idx_on_duty_active", columnList = "is_active"),
        @Index(name = "idx_on_duty_pharmacy", columnList = "pharmacy_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnDutySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    @JsonIgnore
    private Pharmacy pharmacy;

    // Période de garde
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Type de garde
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DutyType dutyType; // WEEKEND, HOLIDAY, NIGHT, SPECIAL

    // Horaires spécifiques
    @Column(name = "start_time")
    private LocalTime startTime; // Ex: 20:00 pour garde de nuit

    @Column(name = "end_time")
    private LocalTime endTime; // Ex: 08:00 lendemain

    // Zone couverte
    @Column(nullable = false)
    private String region;

    private String district;

    // Notes
    @Column(length = 500)
    private String notes; // "Garde partagée avec Pharmacie X"

    // Statut
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy; // Autorité qui a créé la garde

    /**
     * Vérifie si c'est la garde en cours
     */
    @Transient
    public boolean isCurrentlyOnDuty() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate) && Boolean.TRUE.equals(isActive);
    }
}
