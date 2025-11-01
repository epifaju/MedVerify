package com.medverify.service;

import com.medverify.dto.response.DashboardStatsResponse;
import com.medverify.dto.response.SuspiciousScanResponse;
import com.medverify.entity.ReportStatus;
import com.medverify.entity.ScanHistory;
import com.medverify.entity.VerificationStatus;
import com.medverify.repository.ReportRepository;
import com.medverify.repository.ScanHistoryRepository;
import com.medverify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour les statistiques du dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

        private final ScanHistoryRepository scanHistoryRepository;
        private final ReportRepository reportRepository;
        private final UserRepository userRepository;

        /**
         * Récupère les statistiques du dashboard
         */
        public DashboardStatsResponse getStats(String period) {
                log.info("Fetching dashboard stats for period: {}", period);

                Instant[] range = parsePeriod(period);
                Instant start = range[0];
                Instant end = range[1];

                // KPIs principaux
                Long totalScans = scanHistoryRepository.countScansBetween(start, end);

                Map<VerificationStatus, Long> statusCounts = scanHistoryRepository.countByStatus(start, end)
                                .stream()
                                .collect(Collectors.toMap(
                                                row -> (VerificationStatus) row[0],
                                                row -> {
                                                        // FIX: Handle Number type from JPA aggregation
                                                        Object countObj = row[1];
                                                        return countObj instanceof Number
                                                                        ? ((Number) countObj).longValue()
                                                                        : 0L;
                                                }));

                Long authentic = statusCounts.getOrDefault(VerificationStatus.AUTHENTIC, 0L);
                Long suspicious = statusCounts.getOrDefault(VerificationStatus.SUSPICIOUS, 0L);
                Long unknown = statusCounts.getOrDefault(VerificationStatus.UNKNOWN, 0L);

                Double authenticityRate = totalScans > 0
                                ? (authentic.doubleValue() / totalScans) * 100
                                : 0.0;

                // Rapports
                Long totalReports = reportRepository.countBetween(start, end);
                Long confirmedCounterfeits = reportRepository.countByStatusAndPeriod(
                                ReportStatus.CONFIRMED, start, end);

                // Utilisateurs
                Long activeUsers = userRepository.countActiveUsers(start, end);
                Long newUsers = userRepository.countNewUsers(start, end);

                // Tendances (comparaison période précédente)
                Duration periodDuration = Duration.between(start, end);
                Instant prevStart = start.minus(periodDuration);

                Long prevScans = scanHistoryRepository.countScansBetween(prevStart, start);
                Long prevReports = reportRepository.countBetween(prevStart, start);
                Long prevNewUsers = userRepository.countNewUsers(prevStart, start);

                String scansGrowth = calculateGrowthPercentage(totalScans, prevScans);
                String reportsGrowth = calculateGrowthPercentage(totalReports, prevReports);
                String usersGrowth = calculateGrowthPercentage(newUsers, prevNewUsers);

                // Top médicaments contrefaits (simulé pour l'instant)
                List<DashboardStatsResponse.TopCounterfeitMedication> topCounterfeits = getTopCounterfeits(start, end);

                // Distribution géographique (simulée)
                List<DashboardStatsResponse.GeographicDistribution> geoDistribution = getGeographicDistribution(start,
                                end);

                // Alertes récentes
                List<DashboardStatsResponse.RecentAlert> recentAlerts = detectAnomalies(start, end);

                return DashboardStatsResponse.builder()
                                .period(DashboardStatsResponse.Period.builder()
                                                .start(start)
                                                .end(end)
                                                .build())
                                .kpis(DashboardStatsResponse.KPIs.builder()
                                                .totalScans(totalScans)
                                                .authenticMedications(authentic)
                                                .suspiciousMedications(suspicious)
                                                .unknownMedications(unknown)
                                                .authenticityRate(Math.round(authenticityRate * 100.0) / 100.0)
                                                .totalReports(totalReports)
                                                .confirmedCounterfeits(confirmedCounterfeits)
                                                .activeUsers(activeUsers)
                                                .newUsers(newUsers)
                                                .build())
                                .trends(DashboardStatsResponse.Trends.builder()
                                                .scansGrowth(scansGrowth)
                                                .reportsGrowth(reportsGrowth)
                                                .usersGrowth(usersGrowth)
                                                .build())
                                .topCounterfeitMedications(topCounterfeits)
                                .geographicDistribution(geoDistribution)
                                .recentAlerts(recentAlerts)
                                .build();
        }

        /**
         * Parse une période (ex: "30d", "7d", "90d")
         */
        private Instant[] parsePeriod(String period) {
                Instant end = Instant.now();
                Instant start;

                if (period.endsWith("d")) {
                        int days = Integer.parseInt(period.substring(0, period.length() - 1));
                        start = end.minus(days, ChronoUnit.DAYS);
                } else if (period.endsWith("m")) {
                        int months = Integer.parseInt(period.substring(0, period.length() - 1));
                        start = end.minus(months * 30L, ChronoUnit.DAYS);
                } else {
                        start = end.minus(30, ChronoUnit.DAYS); // Par défaut 30 jours
                }

                return new Instant[] { start, end };
        }

        /**
         * Calcule le pourcentage de croissance
         */
        private String calculateGrowthPercentage(Long current, Long previous) {
                if (previous == null || previous == 0) {
                        return current > 0 ? "+100%" : "0%";
                }

                double growth = ((current - previous) / (double) previous) * 100;
                return String.format("%+.1f%%", growth);
        }

        /**
         * Récupère les médicaments les plus contrefaits
         */
        private List<DashboardStatsResponse.TopCounterfeitMedication> getTopCounterfeits(Instant start, Instant end) {
                // Cette implémentation est simplifiée
                // Dans une vraie implémentation, on utiliserait une requête custom du
                // repository

                List<Object[]> suspiciousScans = scanHistoryRepository.findSerialNumberSpikes(start, end, 3);

                return suspiciousScans.stream()
                                .limit(10)
                                .map(row -> {
                                        // FIX: Handle Number type from JPA aggregation
                                        Object countObj = row[1];
                                        Long count = countObj instanceof Number
                                                        ? ((Number) countObj).longValue()
                                                        : 0L;

                                        return DashboardStatsResponse.TopCounterfeitMedication.builder()
                                                        .medicationName((String) row[2])
                                                        .gtin((String) row[3]) // Récupération du GTIN depuis la requête
                                                        .reportCount(count)
                                                        .lastReported(Instant.now())
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        /**
         * Récupère la distribution géographique
         */
        private List<DashboardStatsResponse.GeographicDistribution> getGeographicDistribution(Instant start,
                        Instant end) {
                // Implémentation simplifiée avec données fictives
                // Dans une vraie implémentation, on analyserait les données géospatiales
                // PostGIS

                return List.of(
                                DashboardStatsResponse.GeographicDistribution.builder()
                                                .region("Bissau")
                                                .scans(totalScans(start, end) * 60 / 100)
                                                .reports(5L)
                                                .suspiciousRate(8.2)
                                                .build(),
                                DashboardStatsResponse.GeographicDistribution.builder()
                                                .region("Bafatá")
                                                .scans(totalScans(start, end) * 20 / 100)
                                                .reports(2L)
                                                .suspiciousRate(12.5)
                                                .build(),
                                DashboardStatsResponse.GeographicDistribution.builder()
                                                .region("Gabu")
                                                .scans(totalScans(start, end) * 15 / 100)
                                                .reports(1L)
                                                .suspiciousRate(6.7)
                                                .build(),
                                DashboardStatsResponse.GeographicDistribution.builder()
                                                .region("Autres")
                                                .scans(totalScans(start, end) * 5 / 100)
                                                .reports(0L)
                                                .suspiciousRate(3.1)
                                                .build());
        }

        /**
         * Détecte les anomalies et génère des alertes
         */
        private List<DashboardStatsResponse.RecentAlert> detectAnomalies(Instant start, Instant end) {
                List<DashboardStatsResponse.RecentAlert> alerts = new ArrayList<>();

                // Alerte 1 : Spike de numéros de série dupliqués
                List<Object[]> serialSpikes = scanHistoryRepository.findSerialNumberSpikes(start, end, 10);

                for (Object[] row : serialSpikes) {
                        @SuppressWarnings("unused")
                        String serialNumber = (String) row[0]; // Utilisé pour debug si nécessaire
                        Long count = (Long) row[1];
                        String medicationName = (String) row[2];

                        if (count > 20) {
                                alerts.add(DashboardStatsResponse.RecentAlert.builder()
                                                .id(UUID.randomUUID().toString())
                                                .type("SERIAL_SPIKE")
                                                .severity("HIGH")
                                                .message(String.format("%d scans du même numéro de série détectés",
                                                                count))
                                                .medicationName(medicationName)
                                                .timestamp(Instant.now())
                                                .build());
                        }
                }

                return alerts;
        }

        /**
         * Récupère le total de scans (helper)
         */
        private Long totalScans(Instant start, Instant end) {
                Long scans = scanHistoryRepository.countScansBetween(start, end);
                return scans > 0 ? scans : 100L; // Minimum 100 pour éviter division par 0
        }

        /**
         * Récupère les scans suspects avec pagination
         */
        @Transactional(readOnly = true)
        public List<SuspiciousScanResponse> getSuspiciousScans(int page, int size) {
                log.info("Fetching suspicious scans - page: {}, size: {}", page, size);
                
                // Utiliser findByStatus pour récupérer tous les scans suspects
                // Note: findByStatus ne supporte pas Pageable directement,
                // donc on applique la pagination manuellement
                List<ScanHistory> scansFromDB = scanHistoryRepository.findByStatus(VerificationStatus.SUSPICIOUS);
                
                // FIX: Copy to mutable list to avoid UnsupportedOperationException
                List<ScanHistory> allScans = new ArrayList<>(scansFromDB);
                
                // Trier par date décroissante (les plus récents en premier)
                allScans.sort((s1, s2) -> s2.getScannedAt().compareTo(s1.getScannedAt()));
                
                // Appliquer la pagination
                int start = page * size;
                int end = Math.min(start + size, allScans.size());
                
                if (start >= allScans.size()) {
                        return new ArrayList<>();
                }

                return allScans.subList(start, end).stream()
                                .map(SuspiciousScanResponse::fromScanHistory)
                                .collect(Collectors.toList());
        }
}
