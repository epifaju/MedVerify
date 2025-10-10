package com.medverify.service;

import com.medverify.dto.request.ReportRequest;
import com.medverify.dto.response.ReportResponse;
import com.medverify.entity.*;
import com.medverify.exception.ResourceNotFoundException;
import com.medverify.repository.MedicationRepository;
import com.medverify.repository.ReportRepository;
import com.medverify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des signalements
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MedicationRepository medicationRepository;
    private final EmailService emailService;

    /**
     * Crée un nouveau signalement
     */
    @Transactional
    public ReportResponse createReport(ReportRequest request, String username) {
        log.info("Creating report for GTIN: {} by user: {}", request.getGtin(), username);

        User reporter = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Medication medication = null;
        if (request.getMedicationId() != null) {
            medication = medicationRepository.findById(request.getMedicationId())
                    .orElse(null);
        } else if (request.getGtin() != null) {
            medication = medicationRepository.findByGtin(request.getGtin())
                    .orElse(null);
        }

        // Construire le lieu d'achat
        Report.PurchaseLocation purchaseLocation = null;
        if (request.getPurchaseLocation() != null) {
            purchaseLocation = Report.PurchaseLocation.builder()
                    .name(request.getPurchaseLocation().getName())
                    .address(request.getPurchaseLocation().getAddress())
                    .city(request.getPurchaseLocation().getCity())
                    .region(request.getPurchaseLocation().getRegion())
                    .latitude(request.getPurchaseLocation().getLatitude())
                    .longitude(request.getPurchaseLocation().getLongitude())
                    .build();
        }

        // Générer un numéro de référence unique
        String referenceNumber = generateReferenceNumber();

        Report report = Report.builder()
                .reporter(reporter)
                .medication(medication)
                .gtin(request.getGtin())
                .serialNumber(request.getSerialNumber())
                .reportType(request.getReportType())
                .description(request.getDescription())
                .purchaseLocation(purchaseLocation)
                .photoUrls(request.getPhotoUrls() != null ? request.getPhotoUrls() : new ArrayList<>())
                .anonymous(request.getAnonymous())
                .status(ReportStatus.SUBMITTED)
                .referenceNumber(referenceNumber)
                .build();

        reportRepository.save(report);
        log.info("Report created with reference: {}", referenceNumber);

        // Notifier les autorités
        notifyAuthorities(report);

        // Confirmer au reporter (si non anonyme)
        if (!report.getAnonymous()) {
            sendConfirmationEmail(report);
        }

        return ReportResponse.builder()
                .reportId(report.getId())
                .status(ReportStatus.SUBMITTED)
                .referenceNumber(referenceNumber)
                .message("Signalement enregistré avec succès. Les autorités ont été notifiées.")
                .estimatedProcessingTime("48h")
                .createdAt(report.getCreatedAt())
                .build();
    }

    /**
     * Récupère un signalement par ID
     */
    public ReportResponse.ReportDetailsResponse getReportById(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        return buildReportDetails(report);
    }

    /**
     * Récupère les signalements d'un utilisateur
     */
    public List<ReportResponse.ReportDetailsResponse> getUserReports(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Report> reports = reportRepository.findByReporterIdOrderByCreatedAtDesc(user.getId());

        return reports.stream()
                .map(this::buildReportDetails)
                .toList();
    }

    /**
     * Récupère les signalements par statut (pour autorités)
     */
    public Page<ReportResponse.ReportDetailsResponse> getReportsByStatus(
            ReportStatus status, Pageable pageable) {

        Page<Report> reports = reportRepository.findByStatus(status, pageable);

        return reports.map(this::buildReportDetails);
    }

    /**
     * Met à jour le statut d'un signalement (autorités uniquement)
     */
    @Transactional
    public ReportResponse.ReportDetailsResponse updateReportStatus(
            UUID reportId, ReportStatus newStatus, String reviewNotes, String reviewerUsername) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User reviewer = userRepository.findByEmail(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        report.setStatus(newStatus);
        report.setReviewer(reviewer);
        report.setReviewedAt(Instant.now());
        report.setReviewNotes(reviewNotes);

        reportRepository.save(report);
        log.info("Report {} updated to status: {} by {}", report.getReferenceNumber(), newStatus, reviewerUsername);

        // Notifier le reporter si non anonyme
        if (!report.getAnonymous() && report.getReporter() != null) {
            sendStatusUpdateEmail(report);
        }

        return buildReportDetails(report);
    }

    /**
     * Génère un numéro de référence unique
     */
    private String generateReferenceNumber() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("REP-%s-%s", year, randomPart);
    }

    /**
     * Construit les détails d'un signalement
     */
    private ReportResponse.ReportDetailsResponse buildReportDetails(Report report) {
        return ReportResponse.ReportDetailsResponse.builder()
                .id(report.getId())
                .referenceNumber(report.getReferenceNumber())
                .reportType(report.getReportType())
                .description(report.getDescription())
                .status(report.getStatus())
                .medicationName(report.getMedication() != null ? report.getMedication().getName() : null)
                .gtin(report.getGtin())
                .reporterName(report.getAnonymous() ? "Anonymous"
                        : (report.getReporter() != null ? report.getReporter().getFullName() : null))
                .anonymous(report.getAnonymous())
                .createdAt(report.getCreatedAt())
                .reviewedAt(report.getReviewedAt())
                .reviewNotes(report.getReviewNotes())
                .build();
    }

    /**
     * Notifie les autorités d'un nouveau signalement
     */
    private void notifyAuthorities(Report report) {
        String medicationInfo = report.getMedication() != null
                ? report.getMedication().getName()
                : "GTIN: " + report.getGtin();

        String location = report.getPurchaseLocation() != null
                ? report.getPurchaseLocation().getName() + ", " + report.getPurchaseLocation().getCity()
                : "Non spécifié";

        emailService.sendSuspiciousMedicationAlert(medicationInfo, report.getGtin(), location);
    }

    /**
     * Envoie un email de confirmation au reporter
     */
    private void sendConfirmationEmail(Report report) {
        // Implémentation simplifiée - à enrichir
        log.info("Confirmation email would be sent to reporter for report: {}", report.getReferenceNumber());
    }

    /**
     * Envoie un email de mise à jour de statut
     */
    private void sendStatusUpdateEmail(Report report) {
        // Implémentation simplifiée - à enrichir
        log.info("Status update email would be sent for report: {}", report.getReferenceNumber());
    }
}
