package com.medverify.controller;

import com.medverify.dto.response.DashboardStatsResponse;
import com.medverify.dto.response.ReportResponse;
import com.medverify.entity.ReportStatus;
import com.medverify.service.DashboardService;
import com.medverify.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Contrôleur pour le dashboard des autorités
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Endpoints for authority dashboard and analytics")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ReportService reportService;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get dashboard statistics and KPIs")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @RequestParam(defaultValue = "30d") String period) {
        DashboardStatsResponse stats = dashboardService.getStats(period);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get all reports with filtering")
    public ResponseEntity<Page<ReportResponse.ReportDetailsResponse>> getAllReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ReportResponse.ReportDetailsResponse> reports = status != null
                ? reportService.getReportsByStatus(status, pageable)
                : reportService.getReportsByStatus(ReportStatus.SUBMITTED, pageable);

        return ResponseEntity.ok(reports);
    }

    @PutMapping("/reports/{id}/review")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Review and update report status")
    public ResponseEntity<ReportResponse.ReportDetailsResponse> reviewReport(
            @PathVariable UUID id,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails currentUser) {
        ReportResponse.ReportDetailsResponse response = reportService.updateReportStatus(
                id, status, notes, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }
}
