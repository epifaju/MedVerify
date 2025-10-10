package com.medverify.controller;

import com.medverify.dto.request.ReportRequest;
import com.medverify.dto.response.ReportResponse;
import com.medverify.entity.ReportStatus;
import com.medverify.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur pour les signalements
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for medication reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN')")
    @Operation(summary = "Create a new report for suspicious medication")
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        ReportResponse response = reportService.createReport(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get report by ID")
    public ResponseEntity<ReportResponse.ReportDetailsResponse> getReportById(@PathVariable UUID id) {
        ReportResponse.ReportDetailsResponse response = reportService.getReportById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-reports")
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get current user's reports")
    public ResponseEntity<List<ReportResponse.ReportDetailsResponse>> getMyReports(
            @AuthenticationPrincipal UserDetails currentUser) {
        List<ReportResponse.ReportDetailsResponse> reports = reportService.getUserReports(currentUser.getUsername());
        return ResponseEntity.ok(reports);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get reports by status (authorities only)")
    public ResponseEntity<Page<ReportResponse.ReportDetailsResponse>> getReportsByStatus(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ReportResponse.ReportDetailsResponse> reports = status != null
                ? reportService.getReportsByStatus(status, pageable)
                : reportService.getReportsByStatus(ReportStatus.SUBMITTED, pageable);

        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Update report status (authorities only)")
    public ResponseEntity<ReportResponse.ReportDetailsResponse> updateReportStatus(
            @PathVariable UUID id,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) String reviewNotes,
            @AuthenticationPrincipal UserDetails currentUser) {
        ReportResponse.ReportDetailsResponse response = reportService.updateReportStatus(
                id, status, reviewNotes, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }
}
