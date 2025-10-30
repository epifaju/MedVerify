package com.medverify.service.admin;

import com.medverify.dto.request.CreatePharmacyRequest;
import com.medverify.dto.request.PharmacyAdminSearchRequest;
import com.medverify.dto.request.UpdatePharmacyRequest;
import com.medverify.dto.response.PharmacyAdminDTO;
import com.medverify.dto.response.PharmacyDTO;
import com.medverify.dto.response.PharmacyStatsResponse;
import com.medverify.entity.Pharmacy;
import com.medverify.entity.User;
import com.medverify.entity.UserRole;
import com.medverify.exception.DuplicateResourceException;
import com.medverify.exception.InvalidRequestException;
import com.medverify.exception.ResourceNotFoundException;
import com.medverify.exception.UnauthorizedException;
import com.medverify.repository.PharmacyRepository;
import com.medverify.repository.UserRepository;
import com.medverify.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service admin pour gestion CRUD des pharmacies
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PharmacyAdminService {

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /**
     * Créer une pharmacie
     */
    public PharmacyDTO createPharmacy(CreatePharmacyRequest request, String createdBy) {
        log.info("Creating pharmacy: {}", request.getName());

        // Vérifier si license number unique
        if (request.getLicenseNumber() != null &&
                pharmacyRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("Ce numéro d'agrément existe déjà");
        }

        // Vérifier propriétaire si spécifié
        User owner = null;
        if (request.getOwnerId() != null) {
            owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur propriétaire non trouvé"));

            if (owner.getRole() != UserRole.PHARMACIST) {
                throw new InvalidRequestException("Le propriétaire doit être un pharmacien");
            }
        }

        // Créer Point PostGIS
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude()));

        // Créer pharmacie
        Pharmacy pharmacy = Pharmacy.builder()
                .name(request.getName())
                .licenseNumber(request.getLicenseNumber())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .region(request.getRegion())
                .district(request.getDistrict())
                .country(request.getCountry())
                .location(location)
                .openingHours(request.getOpeningHours())
                .is24h(request.getIs24h())
                .isNightPharmacy(request.getIsNightPharmacy())
                .acceptsEmergencies(request.getAcceptsEmergencies())
                .services(request.getServices())
                .owner(owner)
                .description(request.getDescription())
                .websiteUrl(request.getWebsiteUrl())
                .isVerified(false) // Par défaut non vérifié
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        pharmacy = pharmacyRepository.save(pharmacy);

        // Audit log
        auditLogService.log(
                createdBy,
                "PHARMACY_CREATE",
                "Pharmacy",
                pharmacy.getId(),
                null,
                pharmacy);

        log.info("Pharmacy created successfully: {}", pharmacy.getId());

        return convertToDTO(pharmacy);
    }

    /**
     * Modifier une pharmacie
     */
    public PharmacyDTO updatePharmacy(UUID pharmacyId, UpdatePharmacyRequest request, String updatedBy) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));

        // Vérifier permissions (pharmacien ne peut modifier que SA pharmacie)
        User currentUser = userRepository.findByEmail(updatedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        if (currentUser.getRole() == UserRole.PHARMACIST) {
            if (pharmacy.getOwner() == null || !pharmacy.getOwner().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Vous ne pouvez modifier que votre propre pharmacie");
            }
        }

        // Mettre à jour champs non-null
        if (request.getName() != null)
            pharmacy.setName(request.getName());
        if (request.getLicenseNumber() != null)
            pharmacy.setLicenseNumber(request.getLicenseNumber());
        if (request.getPhoneNumber() != null)
            pharmacy.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null)
            pharmacy.setEmail(request.getEmail());
        if (request.getAddress() != null)
            pharmacy.setAddress(request.getAddress());
        if (request.getCity() != null)
            pharmacy.setCity(request.getCity());
        if (request.getPostalCode() != null)
            pharmacy.setPostalCode(request.getPostalCode());
        if (request.getRegion() != null)
            pharmacy.setRegion(request.getRegion());
        if (request.getDistrict() != null)
            pharmacy.setDistrict(request.getDistrict());

        // Mettre à jour coordonnées si fournies
        if (request.getLatitude() != null && request.getLongitude() != null) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point newLocation = geometryFactory.createPoint(
                    new Coordinate(request.getLongitude(), request.getLatitude()));
            pharmacy.setLocation(newLocation);
        }

        if (request.getOpeningHours() != null)
            pharmacy.setOpeningHours(request.getOpeningHours());
        if (request.getIs24h() != null)
            pharmacy.setIs24h(request.getIs24h());
        if (request.getIsNightPharmacy() != null)
            pharmacy.setIsNightPharmacy(request.getIsNightPharmacy());
        if (request.getAcceptsEmergencies() != null)
            pharmacy.setAcceptsEmergencies(request.getAcceptsEmergencies());
        if (request.getServices() != null)
            pharmacy.setServices(request.getServices());
        if (request.getDescription() != null)
            pharmacy.setDescription(request.getDescription());
        if (request.getWebsiteUrl() != null)
            pharmacy.setWebsiteUrl(request.getWebsiteUrl());

        // Propriétaire (admin/authority uniquement)
        if (request.getOwnerId() != null &&
                (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.AUTHORITY)) {
            User newOwner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Propriétaire non trouvé"));
            pharmacy.setOwner(newOwner);
        }

        pharmacy.setUpdatedAt(Instant.now());
        pharmacy = pharmacyRepository.save(pharmacy);

        // Audit log
        auditLogService.log(
                updatedBy,
                "PHARMACY_UPDATE",
                "Pharmacy",
                pharmacy.getId(),
                null,
                pharmacy);

        log.info("Pharmacy updated: {}", pharmacyId);

        return convertToDTO(pharmacy);
    }

    /**
     * Supprimer (désactiver) une pharmacie
     */
    public void deletePharmacy(UUID pharmacyId, String deletedBy) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));

        pharmacy.setIsActive(false);
        pharmacy.setUpdatedAt(Instant.now());
        pharmacyRepository.save(pharmacy);

        // Audit log
        auditLogService.log(
                deletedBy,
                "PHARMACY_DELETE",
                "Pharmacy",
                pharmacy.getId(),
                null,
                pharmacy);

        log.info("Pharmacy deleted (deactivated): {}", pharmacyId);
    }

    /**
     * Vérifier/Valider une pharmacie
     */
    public PharmacyDTO verifyPharmacy(UUID pharmacyId, boolean verified) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));

        pharmacy.setIsVerified(verified);
        pharmacy.setUpdatedAt(Instant.now());
        pharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy verification status changed: {} = {}", pharmacyId, verified);

        return convertToDTO(pharmacy);
    }

    /**
     * Mettre à jour photo
     */
    public void updatePharmacyPhoto(UUID pharmacyId, String photoUrl) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));

        pharmacy.setPhotoUrl(photoUrl);
        pharmacy.setUpdatedAt(Instant.now());
        pharmacyRepository.save(pharmacy);

        log.info("Pharmacy photo updated: {}", pharmacyId);
    }

    /**
     * Rechercher pharmacies (admin)
     */
    @Transactional(readOnly = true)
    public Page<PharmacyAdminDTO> searchPharmacies(PharmacyAdminSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // Utiliser Specification pour filtres dynamiques
        Specification<Pharmacy> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (request.getCity() != null) {
                predicates.add(cb.equal(cb.lower(root.get("city")), request.getCity().toLowerCase()));
            }

            if (request.getRegion() != null) {
                predicates.add(cb.equal(cb.lower(root.get("region")), request.getRegion().toLowerCase()));
            }

            if (request.getIsVerified() != null) {
                predicates.add(cb.equal(root.get("isVerified"), request.getIsVerified()));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("address")), searchPattern),
                        cb.like(cb.lower(root.get("licenseNumber")), searchPattern)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Pharmacy> pharmaciesPage = pharmacyRepository.findAll(spec, pageable);

        return pharmaciesPage.map(this::convertToAdminDTO);
    }

    /**
     * Statistiques pharmacies
     */
    @Transactional(readOnly = true)
    public PharmacyStatsResponse getPharmacyStats() {
        long totalPharmacies = pharmacyRepository.count();
        long activePharmacies = pharmacyRepository.countByIsActiveTrue();
        long verifiedPharmacies = pharmacyRepository.countByIsVerifiedTrue();
        long pharmacies24h = pharmacyRepository.countByIs24hTrue();
        long nightPharmacies = pharmacyRepository.countByIsNightPharmacyTrue();

        Map<String, Long> pharmaciesByRegion = pharmacyRepository.findAll().stream()
                .collect(Collectors.groupingBy(Pharmacy::getRegion, Collectors.counting()));

        return PharmacyStatsResponse.builder()
                .totalPharmacies(totalPharmacies)
                .activePharmacies(activePharmacies)
                .verifiedPharmacies(verifiedPharmacies)
                .pharmacies24h(pharmacies24h)
                .nightPharmacies(nightPharmacies)
                .pharmaciesByRegion(pharmaciesByRegion)
                .build();
    }

    // Convertir Entity -> DTO
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

    private PharmacyAdminDTO convertToAdminDTO(Pharmacy pharmacy) {
        PharmacyAdminDTO.PharmacyAdminDTOBuilder builder = PharmacyAdminDTO.builder()
                .id(pharmacy.getId())
                .name(pharmacy.getName())
                .licenseNumber(pharmacy.getLicenseNumber())
                .address(pharmacy.getAddress())
                .city(pharmacy.getCity())
                .region(pharmacy.getRegion())
                .district(pharmacy.getDistrict())
                .phoneNumber(pharmacy.getPhoneNumber())
                .email(pharmacy.getEmail())
                .is24h(pharmacy.getIs24h())
                .isNightPharmacy(pharmacy.getIsNightPharmacy())
                .isVerified(pharmacy.getIsVerified())
                .isActive(pharmacy.getIsActive())
                .openingHours(pharmacy.getOpeningHours())
                .services(pharmacy.getServices())
                .ownerId(pharmacy.getOwner() != null ? pharmacy.getOwner().getId() : null)
                .ownerName(pharmacy.getOwner() != null
                        ? pharmacy.getOwner().getFirstName() + " " + pharmacy.getOwner().getLastName()
                        : null)
                .photoUrl(pharmacy.getPhotoUrl())
                .rating(pharmacy.getRating())
                .totalReviews(pharmacy.getTotalReviews())
                .createdAt(pharmacy.getCreatedAt())
                .updatedAt(pharmacy.getUpdatedAt());

        // Extraire coordonnées depuis Point PostGIS
        if (pharmacy.getLocation() != null) {
            Coordinate coord = pharmacy.getLocation().getCoordinate();
            builder.latitude(coord.getY());
            builder.longitude(coord.getX());
        }

        return builder.build();
    }
}
