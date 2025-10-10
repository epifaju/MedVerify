package com.medverify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO pour les statistiques du dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private Period period;
    private KPIs kpis;
    private Trends trends;
    private List<TopCounterfeitMedication> topCounterfeitMedications;
    private List<GeographicDistribution> geographicDistribution;
    private List<RecentAlert> recentAlerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        private Instant start;
        private Instant end;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIs {
        private Long totalScans;
        private Long authenticMedications;
        private Long suspiciousMedications;
        private Long unknownMedications;
        private Double authenticityRate;
        private Long totalReports;
        private Long confirmedCounterfeits;
        private Long activeUsers;
        private Long newUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trends {
        private String scansGrowth;
        private String reportsGrowth;
        private String usersGrowth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCounterfeitMedication {
        private String medicationName;
        private String gtin;
        private Long reportCount;
        private Instant lastReported;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeographicDistribution {
        private String region;
        private Long scans;
        private Long reports;
        private Double suspiciousRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentAlert {
        private String id;
        private String type;
        private String severity;
        private String message;
        private String medicationName;
        private Instant timestamp;
    }
}
