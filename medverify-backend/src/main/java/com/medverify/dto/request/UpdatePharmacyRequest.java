package com.medverify.dto.request;

import com.medverify.entity.OpeningHours;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour mettre à jour une pharmacie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePharmacyRequest {

    private String name;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private String address;
    private String city;
    private String postalCode;
    private String region;
    private String district;

    // Coordonnées GPS (optionnel pour update)
    @Min(-90)
    @Max(90)
    private Double latitude;

    @Min(-180)
    @Max(180)
    private Double longitude;

    private OpeningHours openingHours;

    private Boolean is24h;
    private Boolean isNightPharmacy;
    private Boolean acceptsEmergencies;

    private List<String> services;

    private UUID ownerId;

    private String description;
    private String websiteUrl;

    // Note: isVerified et isActive ne sont pas modifiables via ce DTO
    // gUtiliser endpoints dédiés pour ces champs
}


