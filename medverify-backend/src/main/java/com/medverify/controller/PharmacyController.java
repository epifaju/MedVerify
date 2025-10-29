package com.medverify.controller;

import com.medverify.dto.request.PharmacySearchRequest;
import com.medverify.dto.response.PharmacyDTO;
import com.medverify.service.PharmacyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controller pour la gestion des pharmacies et recherche géolocalisée
 */
@RestController
@RequestMapping("/api/v1/pharmacies")
@RequiredArgsConstructor
@Slf4j
// CORS géré globalement par SecurityConfig
public class PharmacyController {

    private final PharmacyService pharmacyService;

    /**
     * Rechercher pharmacies autour d'une position
     * POST /api/v1/pharmacies/search
     */
    @PostMapping("/search")
    public ResponseEntity<List<PharmacyDTO>> searchPharmacies(
            @Valid @RequestBody PharmacySearchRequest request) {

        log.info("Searching pharmacies: lat={}, lon={}, radius={}km, filters: openNow={}, onDuty={}",
                request.getLatitude(), request.getLongitude(), request.getRadiusKm(),
                request.getOpenNow(), request.getOnDutyOnly());

        List<PharmacyDTO> pharmacies = new ArrayList<>(); // Initialisation pour éviter NullPointerException

        // Recherche par géolocalisation
        if (request.getLatitude() != null && request.getLongitude() != null) {

            if (Boolean.TRUE.equals(request.getOpenNow())) {
                // Pharmacies ouvertes maintenant
                pharmacies = pharmacyService.findOpenPharmaciesNearby(
                        request.getLatitude(),
                        request.getLongitude(),
                        request.getRadiusKm(),
                        request.getLimit());
                log.info("Found {} open pharmacies", pharmacies.size());
            } else if (Boolean.TRUE.equals(request.getOnDutyOnly())) {
                // Pharmacies de garde
                pharmacies = pharmacyService.findOnDutyPharmaciesNearby(
                        request.getLatitude(),
                        request.getLongitude(),
                        request.getRadiusKm(),
                        request.getLimit());
                log.info("Found {} on-duty pharmacies", pharmacies.size());
            } else {
                // Toutes les pharmacies dans le rayon
                pharmacies = pharmacyService.findPharmaciesNearby(
                        request.getLatitude(),
                        request.getLongitude(),
                        request.getRadiusKm(),
                        request.getLimit());
                log.info("Found {} pharmacies within {} km", pharmacies.size(), request.getRadiusKm());
            }

        }
        // Recherche par ville
        else if (request.getCity() != null && !request.getCity().isEmpty()) {
            pharmacies = pharmacyService.searchByCity(request.getCity());
            log.info("Found {} pharmacies in {}", pharmacies.size(), request.getCity());
        }
        // Pharmacies 24h
        else if (Boolean.TRUE.equals(request.getIs24h())) {
            pharmacies = pharmacyService.find24hPharmacies();
            log.info("Found {} 24h pharmacies", pharmacies.size());
        } else {
            log.warn("Invalid search request: no valid search criteria provided");
            return ResponseEntity.badRequest().build();
        }

        // Filtrer par services si spécifié
        if (request.getRequiredServices() != null && !request.getRequiredServices().isEmpty()) {
            int originalSize = pharmacies.size();
            pharmacies = pharmacies.stream()
                    .filter(p -> p.getServices() != null &&
                            p.getServices().containsAll(request.getRequiredServices()))
                    .toList();
            log.info("Filtered by services: {} -> {} pharmacies", originalSize, pharmacies.size());
        }

        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir détails d'une pharmacie
     * GET /api/v1/pharmacies/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PharmacyDTO> getPharmacyDetails(@PathVariable UUID id) {
        log.info("Fetching pharmacy details for id: {}", id);
        PharmacyDTO pharmacy = pharmacyService.getPharmacyDetails(id);
        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Obtenir pharmacies de garde actuelles
     * GET /api/v1/pharmacies/on-duty
     */
    @GetMapping("/on-duty")
    public ResponseEntity<List<PharmacyDTO>> getOnDutyPharmacies(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm,
            @RequestParam(defaultValue = "20") Integer limit) {

        log.info("Fetching on-duty pharmacies: region={}, lat={}, lon={}, radius={}km",
                region, latitude, longitude, radiusKm);

        if (latitude != null && longitude != null) {
            List<PharmacyDTO> pharmacies = pharmacyService.findOnDutyPharmaciesNearby(
                    latitude, longitude, radiusKm, limit);
            return ResponseEntity.ok(pharmacies);
        }

        log.warn("Invalid on-duty request: coordinates required");
        return ResponseEntity.badRequest().build();
    }

    /**
     * Recherche par ville/région
     * GET /api/v1/pharmacies/search-by-city
     */
    @GetMapping("/search-by-city")
    public ResponseEntity<List<PharmacyDTO>> searchByCity(
            @RequestParam String city) {

        log.info("Searching pharmacies in city: {}", city);
        List<PharmacyDTO> pharmacies = pharmacyService.searchByCity(city);
        log.info("Found {} pharmacies in {}", pharmacies.size(), city);

        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir toutes les pharmacies 24h
     * GET /api/v1/pharmacies/24h
     */
    @GetMapping("/24h")
    public ResponseEntity<List<PharmacyDTO>> get24hPharmacies() {
        log.info("Fetching all 24h pharmacies");
        List<PharmacyDTO> pharmacies = pharmacyService.find24hPharmacies();
        log.info("Found {} 24h pharmacies", pharmacies.size());
        return ResponseEntity.ok(pharmacies);
    }
}
