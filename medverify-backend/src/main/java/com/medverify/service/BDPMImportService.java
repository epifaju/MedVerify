package com.medverify.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medverify.entity.Medication;
import com.medverify.integration.BDPMMedicamentMapper;
import com.medverify.integration.dto.BDPMMedicamentResponse;
import com.medverify.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service d'import de la base BDPM complète
 * Télécharge et importe les 15,803 médicaments français
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BDPMImportService {

    private final RestTemplate restTemplate;
    private final MedicationRepository medicationRepository;
    private final BDPMMedicamentMapper bdpmMapper;
    private final TransactionTemplate transactionTemplate;

    @Value("${external-api.medicaments.base-url:https://medicamentsapi.giygas.dev}")
    private String baseUrl;

    @Value("${external-api.bdpm.import-enabled:false}")
    private boolean importEnabled;

    @Value("${external-api.bdpm.page-size:10}")
    private int pageSize;

    private boolean isImporting = false;

    /**
     * Importe la base BDPM complète depuis l'API
     * Peut être appelé manuellement ou via le scheduler
     */
    public void importFullBDPM() {
        if (!importEnabled) {
            log.info("BDPM import is disabled. Set external-api.bdpm.import-enabled=true to enable");
            return;
        }

        if (isImporting) {
            log.warn("Import already in progress, skipping...");
            return;
        }

        isImporting = true;
        Instant startTime = Instant.now();

        try {
            log.info("🚀 Starting BDPM full database import from {}", baseUrl);

            // 1. Obtenir le statut de l'API pour connaître le nombre total
            BDPMHealthResponse health = getHealthStatus();
            if (health == null) {
                log.error("Cannot get API health status, aborting import");
                return;
            }

            int totalMedications = health.getTotalMedications();
            int maxPage = (int) Math.ceil((double) totalMedications / pageSize);

            log.info("📊 Total medications in BDPM: {}, Pages to download: {}", totalMedications, maxPage);

            // 2. Télécharger et importer page par page
            AtomicInteger imported = new AtomicInteger(0);
            AtomicInteger updated = new AtomicInteger(0);
            AtomicInteger errors = new AtomicInteger(0);

            for (int page = 1; page <= maxPage; page++) {
                try {
                    log.info("📥 Downloading page {}/{}", page, maxPage);

                    List<BDPMMedicamentResponse> medications = downloadPageWithRetry(page);

                    if (medications != null && !medications.isEmpty()) {
                        // Importer dans une transaction séparée pour committer chaque page
                        transactionTemplate.execute(status -> {
                            importMedications(medications, imported, updated, errors);
                            return null;
                        });
                    }

                    // Pause entre chaque requête pour éviter le rate limiting
                    // 1500ms = ~0.66 requêtes/seconde (bien en dessous de la limite)
                    Thread.sleep(1500);

                } catch (Exception e) {
                    log.error("Error importing page {}: {}", page, e.getMessage());
                    errors.incrementAndGet();
                    // En cas d'erreur, attendre plus longtemps avant de continuer
                    Thread.sleep(5000);
                }
            }

            Instant endTime = Instant.now();
            long durationSeconds = endTime.getEpochSecond() - startTime.getEpochSecond();

            log.info("✅ BDPM import completed in {}s", durationSeconds);
            log.info("📊 Statistics: {} imported, {} updated, {} errors",
                    imported.get(), updated.get(), errors.get());

        } catch (Exception e) {
            log.error("Fatal error during BDPM import: {}", e.getMessage(), e);
        } finally {
            isImporting = false;
        }
    }

    /**
     * Télécharge une page de médicaments avec retry automatique en cas de rate
     * limiting
     */
    private List<BDPMMedicamentResponse> downloadPageWithRetry(int pageNumber) {
        int maxRetries = 8;
        int retryDelay = 3000; // 3 secondes initialement

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return downloadPage(pageNumber);
            } catch (HttpClientErrorException e) {
                // Gérer toutes les erreurs HTTP client
                boolean isRateLimited = e.getStatusCode().value() == 429 ||
                        e.getMessage().contains("429") ||
                        e.getMessage().contains("Too Many Requests");

                if (isRateLimited) {
                    log.warn("⚠️ Rate limited on page {} (attempt {}/{}), waiting {}ms...",
                            pageNumber, attempt, maxRetries, retryDelay);
                } else {
                    log.error("HTTP error {} on page {}: {}",
                            e.getStatusCode().value(), pageNumber, e.getMessage());
                }

                if (attempt < maxRetries && isRateLimited) {
                    try {
                        Thread.sleep(retryDelay);
                        // Backoff exponentiel
                        retryDelay = Math.min(retryDelay * 2, 30000); // Max 30 secondes
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return List.of();
                    }
                } else if (!isRateLimited || attempt >= maxRetries) {
                    log.error("❌ Failed to download page {} after {} attempts", pageNumber, maxRetries);
                    return List.of();
                }
            } catch (RestClientException e) {
                // Capturer les erreurs de parsing causées par le rate limiting
                boolean likelyRateLimited = e.getMessage().contains("429") ||
                        e.getMessage().contains("Too Many Requests") ||
                        e.getMessage().contains("text/plain");

                if (likelyRateLimited) {
                    log.warn("⚠️ Rate limiting detected (parsing error) on page {} (attempt {}/{}), waiting {}ms...",
                            pageNumber, attempt, maxRetries, retryDelay);

                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(retryDelay);
                            retryDelay = Math.min(retryDelay * 2, 30000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return List.of();
                        }
                    } else {
                        log.error("❌ Failed to download page {} after {} attempts", pageNumber, maxRetries);
                        return List.of();
                    }
                } else {
                    log.error("Error downloading page {}: {}", pageNumber, e.getMessage());
                    return List.of();
                }
            } catch (Exception e) {
                log.error("Unexpected error downloading page {}: {}", pageNumber, e.getMessage());
                return List.of();
            }
        }

        return List.of();
    }

    /**
     * Télécharge une page de médicaments depuis l'API
     */
    private List<BDPMMedicamentResponse> downloadPage(int pageNumber) throws RestClientException {
        String url = baseUrl + "/database/" + pageNumber;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "MedVerify/1.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<BDPMPageResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                BDPMPageResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getData();
        }

        return List.of();
    }

    /**
     * Importe une liste de médicaments dans la base
     * Appelé dans une transaction via TransactionTemplate
     */
    private void importMedications(List<BDPMMedicamentResponse> medications,
            AtomicInteger imported,
            AtomicInteger updated,
            AtomicInteger errors) {
        for (BDPMMedicamentResponse bdpmMed : medications) {
            try {
                Medication medication = bdpmMapper.toEntity(bdpmMed);

                if (medication == null || medication.getGtin() == null) {
                    log.warn("Skipping medication without GTIN: CIS {}", bdpmMed.getCis());
                    continue;
                }

                // Vérifier si existe déjà
                medicationRepository.findByGtin(medication.getGtin())
                        .ifPresentOrElse(
                                existing -> {
                                    // Mettre à jour
                                    updateExisting(existing, medication);
                                    medicationRepository.save(existing);
                                    updated.incrementAndGet();
                                },
                                () -> {
                                    // Créer nouveau
                                    medicationRepository.save(medication);
                                    imported.incrementAndGet();
                                });

            } catch (Exception e) {
                log.error("Error importing medication CIS {}: {}", bdpmMed.getCis(), e.getMessage());
                errors.incrementAndGet();
            }
        }
    }

    /**
     * Met à jour un médicament existant avec les nouvelles données
     */
    private void updateExisting(Medication existing, Medication newData) {
        existing.setCip13(newData.getCip13());
        existing.setCis(newData.getCis());
        existing.setName(newData.getName());
        existing.setGenericName(newData.getGenericName());
        existing.setManufacturer(newData.getManufacturer());
        existing.setDosage(newData.getDosage());
        existing.setPharmaceuticalForm(newData.getPharmaceuticalForm());
        existing.setIsActive(newData.getIsActive());
        existing.setUpdatedAt(Instant.now());

        log.debug("Updated medication: {}", existing.getGtin());
    }

    /**
     * Obtient le statut de l'API
     */
    private BDPMHealthResponse getHealthStatus() {
        try {
            String url = baseUrl + "/health";
            ResponseEntity<BDPMHealthResponse> response = restTemplate.getForEntity(url, BDPMHealthResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error getting API health status: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Job schedulé pour mise à jour automatique quotidienne
     * S'exécute à 3h du matin tous les jours
     */
    @Scheduled(cron = "${external-api.bdpm.import-cron:0 0 3 * * ?}")
    public void scheduledImport() {
        if (!importEnabled) {
            return;
        }

        log.info("⏰ Scheduled BDPM import triggered");
        importFullBDPM();
    }

    /**
     * DTO pour la réponse paginée
     */
    @lombok.Data
    private static class BDPMPageResponse {
        private List<BDPMMedicamentResponse> data;
        private Integer page;
        private Integer pageSize;
        private Integer totalItems;
        private Integer maxPage;
    }

    /**
     * DTO pour le health check
     */
    @lombok.Data
    private static class BDPMHealthResponse {
        private String status;
        private BDPMHealthData data;

        public int getTotalMedications() {
            return data != null ? data.getMedicaments() : 0;
        }
    }

    @lombok.Data
    private static class BDPMHealthData {
        @JsonProperty("medicaments")
        private int medicaments;

        @JsonProperty("generiques")
        private int generiques;
    }
}
