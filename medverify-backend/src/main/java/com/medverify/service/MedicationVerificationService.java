package com.medverify.service;

import com.medverify.dto.request.VerificationRequest;
import com.medverify.dto.response.VerificationResponse;
import com.medverify.entity.*;
import com.medverify.exception.ExternalApiException;
import com.medverify.integration.ApiMedicamentMapper;
import com.medverify.integration.ApiMedicamentsClient;
import com.medverify.integration.dto.ApiMedicamentResponse;
import com.medverify.repository.MedicationBatchRepository;
import com.medverify.repository.MedicationRepository;
import com.medverify.repository.ScanHistoryRepository;
import com.medverify.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service de vérification d'authenticité des médicaments
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MedicationVerificationService {

        private final MedicationRepository medicationRepository;
        private final MedicationBatchRepository batchRepository;
        private final ScanHistoryRepository scanHistoryRepository;
        private final UserRepository userRepository;
        private final MeterRegistry meterRegistry;
        private final EmailService emailService;
        private final ApiMedicamentsClient apiMedicamentsClient;
        private final ApiMedicamentMapper apiMedicamentMapper;
        private final GeometryFactory geometryFactory = new GeometryFactory();

        @Value("${app.verification.duplicate-threshold:5}")
        private int duplicateThreshold;

        @Value("${app.verification.confidence-threshold:0.7}")
        private double confidenceThreshold;

        @Value("${external-api.cache-ttl-days:30}")
        private int cacheTtlDays;

        /**
         * Vérifie l'authenticité d'un médicament
         * Stratégie : Cache local → API-Medicaments.fr → Base locale (fallback)
         */
        public VerificationResponse verify(VerificationRequest request, String username) {
                log.info("Verifying medication with GTIN: {} for user: {}", request.getGtin(), username);

                Timer.Sample sample = Timer.start(meterRegistry);

                try {
                        // 1. Vérifier cache local (base de données)
                        Optional<Medication> cachedMedication = findInCache(request.getGtin());

                        Medication medication;
                        String verificationSource;

                        if (cachedMedication.isPresent() && !isCacheExpired(cachedMedication.get())) {
                                log.info("Medication found in local cache for GTIN: {}", request.getGtin());
                                medication = cachedMedication.get();
                                verificationSource = "CACHE_LOCAL";

                                // Métrique : hit cache
                                meterRegistry.counter("medication.cache.hit").increment();

                        } else {
                                // 2. Appeler API-Medicaments.fr
                                log.info("Cache miss or expired, calling external API for GTIN: {}", request.getGtin());

                                try {
                                        Optional<ApiMedicamentResponse> apiResponse = apiMedicamentsClient
                                                        .findByGtin(request.getGtin());

                                        if (apiResponse.isPresent()) {
                                                log.info("Medication found in API-Medicaments.fr");

                                                // Mapper et sauvegarder en cache
                                                medication = apiMedicamentMapper.toEntity(apiResponse.get());

                                                // Mettre à jour si existe déjà, sinon créer
                                                if (cachedMedication.isPresent()) {
                                                        updateExistingMedication(cachedMedication.get(), medication);
                                                        medication = cachedMedication.get();
                                                }

                                                medication = medicationRepository.save(medication);

                                                verificationSource = "API_MEDICAMENTS_FR";

                                                // Métrique : succès API externe
                                                meterRegistry.counter("external.api.success", "provider",
                                                                "api-medicaments-fr").increment();

                                        } else {
                                                // 3. Fallback : chercher dans base locale existante
                                                log.warn("Medication not found in API, searching local database");
                                                medication = medicationRepository.findByGtin(request.getGtin())
                                                                .orElse(null);
                                                verificationSource = medication != null ? "DB_LOCAL" : "UNKNOWN";

                                                // Métrique : not found
                                                meterRegistry.counter("external.api.not_found", "provider",
                                                                "api-medicaments-fr").increment();
                                        }

                                } catch (ExternalApiException e) {
                                        log.error("External API error, falling back to local DB: {}", e.getMessage());

                                        // Métrique : erreur API
                                        meterRegistry.counter("external.api.error", "provider", "api-medicaments-fr")
                                                        .increment();

                                        // Fallback sur base locale
                                        medication = medicationRepository.findByGtin(request.getGtin()).orElse(null);
                                        verificationSource = medication != null ? "DB_LOCAL_FALLBACK" : "UNKNOWN";
                                }

                                // Métrique : miss cache
                                if (cachedMedication.isEmpty()) {
                                        meterRegistry.counter("medication.cache.miss").increment();
                                }
                        }

                        // 4. Construire la réponse
                        VerificationResponse response;

                        if (medication != null) {
                                response = analyzeAuthenticity(medication, request);
                                response.setVerificationId(java.util.UUID.randomUUID());
                                response.setVerifiedAt(java.time.Instant.now());
                                response.setVerificationSource(verificationSource);

                                // Enrichir avec détails du médicament
                                response.setMedication(buildMedicationDetails(medication));

                                // Notifier si suspect
                                if (response.getStatus() == VerificationStatus.SUSPICIOUS) {
                                        notifySuspicious(medication, request);
                                }

                        } else {
                                response = VerificationResponse.unknown();
                                response.setVerificationSource("NONE");
                                response.setMessage(
                                                "Médicament non trouvé dans les bases de données françaises et locales");
                        }

                        // 5. Enregistrer l'historique du scan
                        saveScanHistory(request, username, medication, response);

                        recordMetrics(response);
                        log.info("Verification completed with status: {} for GTIN: {}", response.getStatus(),
                                        request.getGtin());

                        return response;

                } finally {
                        sample.stop(Timer.builder("scan.verification.duration")
                                        .description("Temps de vérification d'un médicament")
                                        .register(meterRegistry));
                }
        }

        /**
         * Recherche dans le cache local avec gestion des variations de GTIN et CIP13
         */
        private Optional<Medication> findInCache(String gtin) {
                // Essayer d'abord avec le GTIN tel quel
                Optional<Medication> result = medicationRepository.findByGtin(gtin);

                if (result.isEmpty()) {
                        // Essayer avec CIP13 (les 13 derniers chiffres du GTIN)
                        String cip13 = extractCIP13FromGTIN(gtin);
                        if (cip13 != null) {
                                result = medicationRepository.findByCip13(cip13);
                                log.debug("Searching by CIP13: {} (from GTIN: {})", cip13, gtin);
                        }
                }

                if (result.isEmpty() && gtin.length() == 13) {
                        // Si GTIN à 13 chiffres (EAN-13), essayer avec 14 (ajouter 0 devant)
                        result = medicationRepository.findByGtin("0" + gtin);
                }

                return result;
        }

        /**
         * Extrait le CIP13 depuis un GTIN
         * GTIN 14 chiffres → CIP13 = 13 derniers chiffres
         * Ex: 03400922385624 → 3400922385624
         */
        private String extractCIP13FromGTIN(String gtin) {
                if (gtin == null || gtin.length() < 13) {
                        return null;
                }

                if (gtin.length() == 14) {
                        return gtin.substring(1); // Enlever le premier 0
                } else if (gtin.length() == 13) {
                        return gtin; // Déjà au format CIP13
                }

                return null;
        }

        /**
         * Vérifie si la donnée en cache est expirée
         */
        private boolean isCacheExpired(Medication medication) {
                if (medication.getUpdatedAt() == null) {
                        return true;
                }

                Instant expirationDate = medication.getUpdatedAt()
                                .plus(cacheTtlDays, ChronoUnit.DAYS);

                boolean expired = Instant.now().isAfter(expirationDate);

                if (expired) {
                        log.debug("Cache expired for GTIN: {} (last update: {})",
                                        medication.getGtin(), medication.getUpdatedAt());
                }

                return expired;
        }

        /**
         * Met à jour un médicament existant avec les nouvelles données de l'API
         */
        private void updateExistingMedication(Medication existing, Medication apiData) {
                existing.setName(apiData.getName());
                existing.setGenericName(apiData.getGenericName());
                existing.setManufacturer(apiData.getManufacturer());
                existing.setManufacturerCountry(apiData.getManufacturerCountry());
                existing.setDosage(apiData.getDosage());
                existing.setPharmaceuticalForm(apiData.getPharmaceuticalForm());
                existing.setRegistrationNumber(apiData.getRegistrationNumber());
                existing.setIndications(apiData.getIndications());
                existing.setPosology(apiData.getPosology());
                existing.setSideEffects(apiData.getSideEffects());
                existing.setContraindications(apiData.getContraindications());
                existing.setIsActive(apiData.getIsActive());
                existing.setUpdatedAt(Instant.now());

                log.debug("Updated existing medication from API: {}", existing.getGtin());
        }

        /**
         * Analyse l'authenticité du médicament
         */
        private VerificationResponse analyzeAuthenticity(Medication medication, VerificationRequest request) {
                List<VerificationResponse.Alert> alerts = new ArrayList<>();
                double confidence = 1.0;

                // Règle 1 : Vérifier si le GTIN est actif
                if (!medication.getIsActive()) {
                        alerts.add(VerificationResponse.Alert.builder()
                                        .severity("HIGH")
                                        .code("GTIN_INACTIVE")
                                        .message("Ce GTIN n'est plus actif")
                                        .build());
                        confidence -= 0.5;
                }

                // Règle 2 : Numéro de série dupliqué
                if (request.getSerialNumber() != null) {
                        Long duplicateCount = scanHistoryRepository.countBySerialNumber(request.getSerialNumber());
                        if (duplicateCount >= duplicateThreshold) {
                                alerts.add(VerificationResponse.Alert.builder()
                                                .severity("HIGH")
                                                .code("SERIAL_DUPLICATE")
                                                .message(String.format("Numéro de série scanné %d fois (seuil : %d)",
                                                                duplicateCount, duplicateThreshold))
                                                .build());
                                confidence -= 0.6;
                        }
                }

                // Règle 3 : Date de péremption
                if (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDate.now())) {
                        alerts.add(VerificationResponse.Alert.builder()
                                        .severity("MEDIUM")
                                        .code("EXPIRED")
                                        .message("Médicament périmé")
                                        .build());
                        confidence -= 0.3;
                }

                // Règle 4 : Lot rappelé
                if (request.getBatchNumber() != null) {
                        Optional<MedicationBatch> batch = batchRepository.findByBatchNumberAndMedicationId(
                                        request.getBatchNumber(), medication.getId());

                        if (batch.isPresent() && batch.get().getIsRecalled()) {
                                alerts.add(VerificationResponse.Alert.builder()
                                                .severity("CRITICAL")
                                                .code("BATCH_RECALLED")
                                                .message(String.format("Lot rappelé : %s",
                                                                batch.get().getRecallReason()))
                                                .build());
                                confidence -= 0.8;
                        }
                }

                // Déterminer le statut final
                VerificationStatus status = confidence >= confidenceThreshold
                                ? VerificationStatus.AUTHENTIC
                                : VerificationStatus.SUSPICIOUS;

                return VerificationResponse.builder()
                                .status(status)
                                .confidence(Math.max(0, confidence))
                                .alerts(alerts)
                                .build();
        }

        /**
         * Construit les détails du médicament pour la réponse
         */
        private VerificationResponse.MedicationDetails buildMedicationDetails(Medication medication) {
                VerificationResponse.PosologyDetails posology = null;
                if (medication.getPosology() != null) {
                        posology = VerificationResponse.PosologyDetails.builder()
                                        .adult(medication.getPosology().getAdult())
                                        .child(medication.getPosology().getChild())
                                        .maxDailyDose(medication.getPosology().getMaxDailyDose())
                                        .unit(medication.getPosology().getUnit())
                                        .frequency(medication.getPosology().getFrequency())
                                        .build();
                }

                return VerificationResponse.MedicationDetails.builder()
                                .id(medication.getId())
                                .gtin(medication.getGtin())
                                .name(medication.getName())
                                .genericName(medication.getGenericName())
                                .manufacturer(medication.getManufacturer())
                                .dosage(medication.getDosage())
                                .pharmaceuticalForm(medication.getPharmaceuticalForm())
                                .indications(medication.getIndications())
                                .sideEffects(medication.getSideEffects())
                                .contraindications(medication.getContraindications())
                                .posology(posology)
                                .isEssential(medication.getIsEssential())
                                .imageUrl(medication.getImageUrl())
                                .build();
        }

        /**
         * Sauvegarde le scan dans l'historique
         */
        private void saveScanHistory(VerificationRequest request, String username,
                        Medication medication, VerificationResponse response) {
                User user = userRepository.findByEmail(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Point location = null;
                if (request.getLocation() != null &&
                                request.getLocation().getLatitude() != null &&
                                request.getLocation().getLongitude() != null) {
                        location = geometryFactory.createPoint(new Coordinate(
                                        request.getLocation().getLongitude(),
                                        request.getLocation().getLatitude()));
                        location.setSRID(4326);
                }

                ScanHistory.DeviceInfo deviceInfo = null;
                if (request.getDeviceInfo() != null) {
                        deviceInfo = ScanHistory.DeviceInfo.builder()
                                        .platform(request.getDeviceInfo().getPlatform())
                                        .osVersion(request.getDeviceInfo().getOsVersion())
                                        .appVersion(request.getDeviceInfo().getAppVersion())
                                        .deviceModel(request.getDeviceInfo().getDeviceModel())
                                        .build();
                }

                List<ScanHistory.Alert> historyAlerts = response.getAlerts() != null
                                ? response.getAlerts().stream()
                                                .map(alert -> ScanHistory.Alert.builder()
                                                                .severity(alert.getSeverity())
                                                                .code(alert.getCode())
                                                                .message(alert.getMessage())
                                                                .build())
                                                .toList()
                                : new ArrayList<>();

                ScanHistory scanHistory = ScanHistory.builder()
                                .user(user)
                                .medication(medication)
                                .gtin(request.getGtin())
                                .serialNumber(request.getSerialNumber())
                                .batchNumber(request.getBatchNumber())
                                .status(response.getStatus())
                                .confidence(response.getConfidence())
                                .alerts(historyAlerts)
                                .location(location)
                                .deviceInfo(deviceInfo)
                                .build();

                scanHistoryRepository.save(scanHistory);
                log.debug("Scan history saved for GTIN: {}", request.getGtin());
        }

        /**
         * Notifie les autorités d'un médicament suspect
         */
        private void notifySuspicious(Medication medication, VerificationRequest request) {
                String location = request.getLocation() != null
                                ? String.format("Lat: %.4f, Lon: %.4f",
                                                request.getLocation().getLatitude(),
                                                request.getLocation().getLongitude())
                                : "Unknown";

                emailService.sendSuspiciousMedicationAlert(
                                medication.getName(),
                                medication.getGtin(),
                                location);

                log.warn("Suspicious medication alert sent for: {}", medication.getName());
        }

        /**
         * Enregistre les métriques Prometheus
         */
        private void recordMetrics(VerificationResponse response) {
                meterRegistry.counter(
                                "scan.verification",
                                "status", response.getStatus().toString(),
                                "source",
                                response.getVerificationSource() != null ? response.getVerificationSource() : "UNKNOWN")
                                .increment();
        }
}
