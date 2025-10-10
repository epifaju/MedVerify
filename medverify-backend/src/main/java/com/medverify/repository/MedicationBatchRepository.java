package com.medverify.repository;

import com.medverify.entity.MedicationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité MedicationBatch
 */
@Repository
public interface MedicationBatchRepository extends JpaRepository<MedicationBatch, UUID> {

    /**
     * Trouve un lot par numéro de lot et medication
     */
    Optional<MedicationBatch> findByBatchNumberAndMedicationId(String batchNumber, UUID medicationId);

    /**
     * Trouve tous les lots rappelés
     */
    List<MedicationBatch> findByIsRecalledTrue();

    /**
     * Trouve les lots expirés
     */
    @Query("SELECT b FROM MedicationBatch b WHERE b.expiryDate < :date")
    List<MedicationBatch> findExpiredBatches(@Param("date") LocalDate date);

    /**
     * Trouve les lots d'un médicament
     */
    List<MedicationBatch> findByMedicationId(UUID medicationId);
}

