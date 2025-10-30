package com.medverify.repository;

import com.medverify.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour gérer les pharmacies avec support PostGIS
 */
@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, UUID>, JpaSpecificationExecutor<Pharmacy> {

    // Recherche par ville
    List<Pharmacy> findByCityIgnoreCaseAndIsActiveTrue(String city);

    // Recherche par région
    List<Pharmacy> findByRegionIgnoreCaseAndIsActiveTrue(String region);

    // Pharmacies 24h
    List<Pharmacy> findByIs24hTrueAndIsActiveTrue();

    // Pharmacies de nuit
    List<Pharmacy> findByIsNightPharmacyTrueAndIsActiveTrue();

    // Par propriétaire (pharmacien)
    List<Pharmacy> findByOwnerIdAndIsActiveTrue(UUID ownerId);

    // REQUÊTES GÉOSPATIALES avec PostGIS

    /**
     * Trouver pharmacies dans un rayon (en mètres)
     * Utilise ST_DWithin de PostGIS
     */
    @Query(value = """
                SELECT p.*,
                       ST_Distance(p.location\\:\\:geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography) as distance
                FROM pharmacies p
                WHERE p.is_active = true
                AND ST_DWithin(
                    p.location\\:\\:geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography,
                    :radiusMeters
                )
                ORDER BY distance
                LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findPharmaciesWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit);

    /**
     * Trouver N pharmacies les plus proches
     */
    @Query(value = """
                SELECT p.*,
                       ST_Distance(p.location\\:\\:geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography) as distance
                FROM pharmacies p
                WHERE p.is_active = true
                ORDER BY p.location <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geometry
                LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findNearestPharmacies(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("limit") int limit);

    /**
     * Trouver pharmacies 24h dans un rayon
     */
    @Query(value = """
                SELECT p.*,
                       ST_Distance(p.location\\:\\:geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography) as distance
                FROM pharmacies p
                WHERE p.is_active = true
                AND p.is_24h = true
                AND ST_DWithin(
                    p.location\\:\\:geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography,
                    :radiusMeters
                )
                ORDER BY distance
                LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> find24hPharmaciesNearby(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit);

    // Méthodes pour statistiques admin
    Long countByIsActiveTrue();

    Long countByIsVerifiedTrue();

    Long countByIs24hTrue();

    Long countByIsNightPharmacyTrue();

    // Vérifier existence
    boolean existsByLicenseNumber(String licenseNumber);
}
