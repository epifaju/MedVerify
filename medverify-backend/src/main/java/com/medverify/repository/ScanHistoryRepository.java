package com.medverify.repository;

import com.medverify.entity.ScanHistory;
import com.medverify.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité ScanHistory
 */
@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {

        /**
         * Trouve l'historique d'un utilisateur
         */
        List<ScanHistory> findByUserIdOrderByScannedAtDesc(UUID userId);

        /**
         * Compte les scans d'un numéro de série
         * @deprecated Utiliser countUniqueUsersBySerialAndGtin pour une détection plus précise
         */
        @Query("SELECT COUNT(s) FROM ScanHistory s WHERE s.serialNumber = :serialNumber")
        Long countBySerialNumber(@Param("serialNumber") String serialNumber);

        /**
         * Compte le nombre d'utilisateurs uniques ayant scanné un numéro de série pour un GTIN donné
         * dans une période donnée, en excluant l'utilisateur actuel.
         * Utilisé pour détecter les numéros de série dupliqués (contrefaçons).
         *
         * @param serialNumber Le numéro de série à vérifier
         * @param gtin Le GTIN du médicament
         * @param periodStart Date de début de la période (ex: 30 derniers jours)
         * @param excludeUserId L'ID de l'utilisateur à exclure (utilisateur actuel qui scanne)
         * @return Le nombre d'utilisateurs uniques ayant scanné ce numéro de série
         */
        @Query("SELECT COUNT(DISTINCT s.user.id) FROM ScanHistory s " +
                        "WHERE s.serialNumber = :serialNumber " +
                        "AND s.gtin = :gtin " +
                        "AND s.scannedAt >= :periodStart " +
                        "AND s.user.id != :excludeUserId")
        Long countUniqueUsersBySerialAndGtin(
                        @Param("serialNumber") String serialNumber,
                        @Param("gtin") String gtin,
                        @Param("periodStart") Instant periodStart,
                        @Param("excludeUserId") UUID excludeUserId);

        /**
         * Compte les scans dans une période
         */
        @Query("SELECT COUNT(s) FROM ScanHistory s WHERE s.scannedAt BETWEEN :start AND :end")
        Long countScansBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Compte les scans par statut dans une période
         */
        @Query("SELECT s.status, COUNT(s) FROM ScanHistory s " +
                        "WHERE s.scannedAt BETWEEN :start AND :end " +
                        "GROUP BY s.status")
        List<Object[]> countByStatus(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Trouve les top numéros de série suspects (plus scannés)
         */
        @Query("SELECT s.serialNumber, COUNT(s), s.medication.name, s.gtin FROM ScanHistory s " +
                        "WHERE s.scannedAt BETWEEN :start AND :end " +
                        "GROUP BY s.serialNumber, s.medication.name, s.gtin " +
                        "HAVING COUNT(s) > :threshold " +
                        "ORDER BY COUNT(s) DESC")
        List<Object[]> findSerialNumberSpikes(
                        @Param("start") Instant start,
                        @Param("end") Instant end,
                        @Param("threshold") int threshold);

        /**
         * Trouve les scans par statut
         */
        List<ScanHistory> findByStatus(VerificationStatus status);

        /**
         * Trouve les scans suspects récents
         */
        @Query("SELECT s FROM ScanHistory s WHERE s.status = 'SUSPICIOUS' " +
                        "ORDER BY s.scannedAt DESC")
        List<ScanHistory> findRecentSuspiciousScans();
}
