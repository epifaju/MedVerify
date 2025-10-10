package com.medverify.repository;

import com.medverify.entity.Report;
import com.medverify.entity.ReportStatus;
import com.medverify.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Report
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    /**
     * Trouve les signalements d'un utilisateur
     */
    List<Report> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);

    /**
     * Trouve les signalements par statut
     */
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * Trouve les signalements par type
     */
    List<Report> findByReportType(ReportType reportType);

    /**
     * Compte les signalements dans une période
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt BETWEEN :start AND :end")
    Long countBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Compte les signalements par statut dans une période
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status " +
            "AND r.createdAt BETWEEN :start AND :end")
    Long countByStatusAndPeriod(
            @Param("status") ReportStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Trouve les signalements récents
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'SUBMITTED' " +
            "ORDER BY r.createdAt DESC")
    List<Report> findRecentSubmittedReports(Pageable pageable);

    /**
     * Trouve par numéro de référence
     */
    Report findByReferenceNumber(String referenceNumber);

    /**
     * Compte les signalements par type dans une période
     */
    @Query("SELECT r.reportType, COUNT(r) FROM Report r " +
            "WHERE r.createdAt BETWEEN :start AND :end " +
            "GROUP BY r.reportType")
    List<Object[]> countByTypeAndPeriod(@Param("start") Instant start, @Param("end") Instant end);
}
