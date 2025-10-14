package com.medverify.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour les critères de recherche de pharmacies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacySearchRequest {

    // Géolocalisation
    @Min(-90)
    @Max(90)
    private Double latitude;

    @Min(-180)
    @Max(180)
    private Double longitude;

    // Rayon de recherche (km)
    @Min(1)
    @Max(50)
    private Double radiusKm = 5.0;

    // Limite résultats
    @Min(1)
    @Max(100)
    private Integer limit = 20;

    // Filtres
    private String city;
    private String region;
    private Boolean openNow; // Seulement pharmacies ouvertes
    private Boolean onDutyOnly; // Seulement pharmacies de garde
    private Boolean is24h; // Seulement pharmacies 24h
    private Boolean nightOnly; // Seulement pharmacies de nuit

    // Services requis
    private List<String> requiredServices; // "DELIVERY", "COVID_TEST", etc.
}
