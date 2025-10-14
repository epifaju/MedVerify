package com.medverify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une pharmacie avec géolocalisation
 */
@Entity
@Table(name = "pharmacies", indexes = {
        @Index(name = "idx_pharmacies_city", columnList = "city"),
        @Index(name = "idx_pharmacies_region", columnList = "region"),
        @Index(name = "idx_pharmacies_active", columnList = "is_active"),
        @Index(name = "idx_pharmacies_24h", columnList = "is_24h"),
        @Index(name = "idx_pharmacies_night", columnList = "is_night_pharmacy")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Informations de base
    @Column(nullable = false)
    private String name;

    @Column(length = 20, unique = true)
    private String licenseNumber; // Numéro agrément

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    // Adresse
    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(nullable = false)
    private String region; // "Bissau", "Bafatá", etc.

    private String district; // Quartier

    @Column(nullable = false)
    @Builder.Default
    private String country = "Guinée-Bissau";

    // Géolocalisation (PostGIS)
    @Column(columnDefinition = "geography(Point, 4326)", nullable = false)
    private Point location; // Longitude, Latitude

    // Horaires d'ouverture (JSON)
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private OpeningHours openingHours;

    // Type de pharmacie
    @Column(name = "is_24h")
    @Builder.Default
    private Boolean is24h = false; // Pharmacie 24h/24

    @Column(name = "is_night_pharmacy")
    @Builder.Default
    private Boolean isNightPharmacy = false; // Ouverte jusqu'à minuit+

    @Column(name = "accepts_emergencies")
    @Builder.Default
    private Boolean acceptsEmergencies = true;

    // Services disponibles
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pharmacy_services", joinColumns = @JoinColumn(name = "pharmacy_id"))
    @Column(name = "service")
    private List<String> services; // "DELIVERY", "HOME_VISIT", "COVID_TEST", etc.

    // Responsable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner; // Pharmacien propriétaire

    // Statut
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false; // Vérifié par autorités

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Photo et description
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(length = 1000)
    private String description;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    // Notation (future fonctionnalité)
    @Column(name = "rating")
    private Double rating; // 0-5

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    // Métadonnées
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Méthode utilitaire : calculer distance
    @Transient
    private Double distanceKm; // Distance depuis position utilisateur (calculée dynamiquement)

    /**
     * Vérifie si la pharmacie est ouverte maintenant
     */
    @Transient
    public boolean isOpenNow() {
        if (is24h != null && is24h)
            return true;
        if (openingHours == null)
            return false;

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        OpeningHoursDay todayHours = openingHours.getDay(day);
        if (todayHours == null || todayHours.isClosed())
            return false;

        return todayHours.isOpenAt(time);
    }
}
