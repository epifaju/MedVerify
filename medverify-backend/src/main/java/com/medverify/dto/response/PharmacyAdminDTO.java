package com.medverify.dto.response;

import com.medverify.entity.OpeningHours;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour représentation admin d'une pharmacie (avec informations sensibles)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyAdminDTO {

    private UUID id;
    private String name;
    private String licenseNumber;
    private String address;
    private String city;
    private String region;
    private String district;
    private String phoneNumber;
    private String email;

    private Double latitude;
    private Double longitude;

    private Boolean is24h;
    private Boolean isNightPharmacy;
    private Boolean isVerified;
    private Boolean isActive;

    private OpeningHours openingHours;
    private List<String> services;

    private UUID ownerId;
    private String ownerName;

    private String photoUrl;
    private Double rating;
    private Integer totalReviews;

    private Instant createdAt;
    private Instant updatedAt;
}


