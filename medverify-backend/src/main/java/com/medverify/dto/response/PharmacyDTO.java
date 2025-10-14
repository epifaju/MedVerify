package com.medverify.dto.response;

import com.medverify.entity.OpeningHours;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour représenter une pharmacie dans les réponses API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyDTO {

    private UUID id;
    private String name;
    private String address;
    private String city;
    private String region;
    private String district;
    private String phoneNumber;
    private String email;

    // Coordonnées
    private Double latitude;
    private Double longitude;

    // Distance depuis position utilisateur (calculée)
    private Double distanceKm;

    // Type
    private Boolean is24h;
    private Boolean isNightPharmacy;
    private Boolean isOpenNow;

    // Horaires
    private OpeningHours openingHours;

    // Services
    private List<String> services;

    // Garde
    @Builder.Default
    private Boolean isOnDuty = false;
    private String onDutyInfo; // "WEEKEND", "NIGHT", etc.

    // Infos supplémentaires
    private String photoUrl;
    private Double rating;
    private Integer totalReviews;
}
