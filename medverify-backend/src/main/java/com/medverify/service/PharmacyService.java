package com.medverify.service;

import com.medverify.dto.response.PharmacyDTO;
import com.medverify.entity.OnDutySchedule;
import com.medverify.entity.Pharmacy;
import com.medverify.exception.ResourceNotFoundException;
import com.medverify.repository.OnDutyScheduleRepository;
import com.medverify.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour gérer les pharmacies et leur géolocalisation
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final OnDutyScheduleRepository onDutyScheduleRepository;

    /**
     * Trouver pharmacies autour d'une position
     */
    public List<PharmacyDTO> findPharmaciesNearby(double latitude, double longitude,
            double radiusKm, int limit) {
        double radiusMeters = radiusKm * 1000;

        List<Object[]> results = pharmacyRepository.findPharmaciesWithinRadius(
                latitude, longitude, radiusMeters, limit);

        return results.stream()
                .map(this::mapToPharmacyDTO)
                .collect(Collectors.toList());
    }

    /**
     * Trouver pharmacies ouvertes maintenant autour d'une position
     */
    public List<PharmacyDTO> findOpenPharmaciesNearby(double latitude, double longitude,
            double radiusKm, int limit) {
        double radiusMeters = radiusKm * 1000;

        List<Object[]> results = pharmacyRepository.findPharmaciesWithinRadius(
                latitude, longitude, radiusMeters, limit * 2 // Chercher plus car on va filtrer
        );

        return results.stream()
                .map(this::mapToPharmacyDTO)
                .filter(dto -> Boolean.TRUE.equals(dto.getIsOpenNow()) || Boolean.TRUE.equals(dto.getIs24h()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Trouver pharmacies de garde actuelles autour d'une position
     */
    public List<PharmacyDTO> findOnDutyPharmaciesNearby(double latitude, double longitude,
            double radiusKm, int limit) {
        double radiusMeters = radiusKm * 1000;
        LocalDate today = LocalDate.now();

        List<Object[]> results = onDutyScheduleRepository.findOnDutyPharmaciesNearby(
                today, latitude, longitude, radiusMeters, limit);

        return results.stream()
                .map(this::mapOnDutyPharmacyDTO)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher pharmacies par ville
     */
    public List<PharmacyDTO> searchByCity(String city) {
        List<Pharmacy> pharmacies = pharmacyRepository
                .findByCityIgnoreCaseAndIsActiveTrue(city);

        return pharmacies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Trouver pharmacies 24h
     */
    public List<PharmacyDTO> find24hPharmacies() {
        List<Pharmacy> pharmacies = pharmacyRepository.findByIs24hTrueAndIsActiveTrue();
        return pharmacies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir détails pharmacie
     */
    public PharmacyDTO getPharmacyDetails(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found"));

        PharmacyDTO dto = convertToDTO(pharmacy);

        // Ajouter info garde si active aujourd'hui
        LocalDate today = LocalDate.now();
        List<OnDutySchedule> schedules = onDutyScheduleRepository
                .findActiveSchedulesForDate(today).stream()
                .filter(s -> s.getPharmacy().getId().equals(pharmacyId))
                .collect(Collectors.toList());

        if (!schedules.isEmpty()) {
            dto.setIsOnDuty(true);
            dto.setOnDutyInfo(schedules.get(0).getDutyType().name());
        }

        return dto;
    }

    /**
     * Mapper Object[] résultat requête native -> DTO
     */
    private PharmacyDTO mapToPharmacyDTO(Object[] row) {
        Pharmacy pharmacy = (Pharmacy) row[0];
        Double distanceMeters = ((Number) row[1]).doubleValue();

        PharmacyDTO dto = convertToDTO(pharmacy);
        dto.setDistanceKm(distanceMeters / 1000.0);

        return dto;
    }

    /**
     * Mapper résultat garde -> DTO (requête native)
     */
    private PharmacyDTO mapOnDutyPharmacyDTO(Object[] row) {
        // Structure de la requête native:
        // p.id, p.name, p.address, p.city, p.region, p.district, p.phone_number,
        // p.email, p.is_24h, p.is_night_pharmacy, p.is_active, p.photo_url, p.rating,
        // longitude, latitude, distance, duty_type, start_date, end_date

        PharmacyDTO dto = PharmacyDTO.builder()
                .id(UUID.fromString(row[0].toString()))
                .name((String) row[1])
                .address((String) row[2])
                .city((String) row[3])
                .region((String) row[4])
                .district((String) row[5])
                .phoneNumber((String) row[6])
                .email((String) row[7])
                .is24h((Boolean) row[8])
                .isNightPharmacy((Boolean) row[9])
                .photoUrl((String) row[11])
                .rating(row[12] != null ? ((Number) row[12]).doubleValue() : null)
                .longitude(row[13] != null ? ((Number) row[13]).doubleValue() : null)
                .latitude(row[14] != null ? ((Number) row[14]).doubleValue() : null)
                .distanceKm(row[15] != null ? ((Number) row[15]).doubleValue() / 1000.0 : null)
                .isOnDuty(true)
                .onDutyInfo((String) row[16])
                .build();

        return dto;
    }

    /**
     * Convertir Entity -> DTO
     */
    private PharmacyDTO convertToDTO(Pharmacy pharmacy) {
        PharmacyDTO.PharmacyDTOBuilder builder = PharmacyDTO.builder()
                .id(pharmacy.getId())
                .name(pharmacy.getName())
                .address(pharmacy.getAddress())
                .city(pharmacy.getCity())
                .region(pharmacy.getRegion())
                .district(pharmacy.getDistrict())
                .phoneNumber(pharmacy.getPhoneNumber())
                .email(pharmacy.getEmail())
                .is24h(pharmacy.getIs24h())
                .isNightPharmacy(pharmacy.getIsNightPharmacy())
                .isOpenNow(pharmacy.isOpenNow())
                .openingHours(pharmacy.getOpeningHours())
                .services(pharmacy.getServices())
                .photoUrl(pharmacy.getPhotoUrl())
                .rating(pharmacy.getRating())
                .totalReviews(pharmacy.getTotalReviews());

        // Extraire coordonnées depuis Point PostGIS
        if (pharmacy.getLocation() != null) {
            Coordinate coord = pharmacy.getLocation().getCoordinate();
            builder.latitude(coord.getY());
            builder.longitude(coord.getX());
        }

        return builder.build();
    }
}
