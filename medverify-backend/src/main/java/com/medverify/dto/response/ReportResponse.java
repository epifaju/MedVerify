package com.medverify.dto.response;

import com.medverify.entity.ReportStatus;
import com.medverify.entity.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO pour la réponse de signalement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private UUID reportId;
    private ReportStatus status;
    private String referenceNumber;
    private String message;
    private String estimatedProcessingTime;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportDetailsResponse {
        private UUID id;
        private String referenceNumber;
        private ReportType reportType;
        private String description;
        private ReportStatus status;
        private String medicationName;
        private String gtin;
        private String reporterName;
        private Boolean anonymous;
        private Instant createdAt;
        private Instant reviewedAt;
        private String reviewNotes;
    }
}
