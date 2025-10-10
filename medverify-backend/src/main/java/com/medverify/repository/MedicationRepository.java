package com.medverify.repository;

import com.medverify.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Medication
 */
@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    /**
     * Trouve un médicament par GTIN
     */
    Optional<Medication> findByGtin(String gtin);

    /**
     * Vérifie si un GTIN existe
     */
    boolean existsByGtin(String gtin);

    /**
     * Trouve un médicament par CIP13 (Code Identifiant Présentation 13 chiffres)
     */
    Optional<Medication> findByCip13(String cip13);

    /**
     * Trouve un médicament par CIS (Code Identifiant Spécialité)
     */
    Optional<Medication> findByCis(Long cis);

    /**
     * Trouve tous les médicaments essentiels
     */
    List<Medication> findByIsEssentialTrue();

    /**
     * Trouve tous les médicaments actifs
     */
    List<Medication> findByIsActiveTrue();

    /**
     * Recherche par nom (insensible à la casse)
     */
    @Query("SELECT m FROM Medication m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Medication> searchByName(@Param("name") String name);

    /**
     * Trouve par fabricant
     */
    List<Medication> findByManufacturer(String manufacturer);

    /**
     * Trouve par code ATC
     */
    List<Medication> findByAtcCodeStartingWith(String atcCode);
}
