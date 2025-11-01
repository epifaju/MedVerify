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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MedicationVerificationService
 */
@ExtendWith(MockitoExtension.class)
class MedicationVerificationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private MedicationBatchRepository batchRepository;

    @Mock
    private ScanHistoryRepository scanHistoryRepository;

    @Mock
    private UserRepository userRepository;

    private MeterRegistry meterRegistry;

    @Mock
    private EmailService emailService;

    @Mock
    private ApiMedicamentsClient apiMedicamentsClient;

    @Mock
    private ApiMedicamentMapper apiMedicamentMapper;

    @InjectMocks
    private MedicationVerificationService verificationService;

    private Medication testMedication;
    private VerificationRequest testRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Utiliser SimpleMeterRegistry au lieu d'un mock pour éviter les problèmes avec Timer.start()
        meterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(verificationService, "meterRegistry", meterRegistry);

        // Configuration des valeurs par défaut via ReflectionTestUtils
        ReflectionTestUtils.setField(verificationService, "duplicateThreshold", 5);
        ReflectionTestUtils.setField(verificationService, "duplicateThresholdUsers", 3);
        ReflectionTestUtils.setField(verificationService, "duplicatePeriodDays", 30);
        ReflectionTestUtils.setField(verificationService, "confidenceThreshold", 0.7);
        ReflectionTestUtils.setField(verificationService, "cacheTtlDays", 30);

        // Créer un médicament de test
        testMedication = Medication.builder()
                .id(UUID.randomUUID())
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .genericName("Paracétamol")
                .manufacturer("PharmaCare Labs")
                .manufacturerCountry("France")
                .dosage("500mg")
                .pharmaceuticalForm("Comprimé")
                .isActive(true)
                .isEssential(true)
                .updatedAt(Instant.now())
                .build();

        // Créer une requête de test
        testRequest = VerificationRequest.builder()
                .gtin("03401234567890")
                .serialNumber("SN123456")
                .batchNumber("LOT2024A123")
                .expiryDate(LocalDate.now().plusYears(2))
                .build();

        // Créer un utilisateur de test
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.PATIENT)
                .isVerified(true)
                .isActive(true)
                .build();
    }

    @Test
    void verify_ValidGtinInCache_ShouldReturnAuthentic() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(testMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(eq("SN123456"), eq("03401234567890"), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.AUTHENTIC);
        assertThat(response.getVerificationSource()).isEqualTo("CACHE_LOCAL");
        assertThat(response.getConfidence()).isGreaterThanOrEqualTo(0.7);
        assertThat(response.getMedication()).isNotNull();
        assertThat(response.getMedication().getGtin()).isEqualTo("03401234567890");

        // Vérifier que le cache hit est enregistré
        Counter cacheHitCounter = meterRegistry.counter("medication.cache.hit");
        assertThat(cacheHitCounter.count()).isGreaterThan(0);
        verify(scanHistoryRepository).save(any(ScanHistory.class));
    }

    @Test
    void verify_CacheMiss_ShouldCallApiAndCacheResult() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.empty());

        ApiMedicamentResponse apiResponse = new ApiMedicamentResponse();
        apiResponse.setGtin("03401234567890");
        apiResponse.setName("Paracétamol 500mg");

        when(apiMedicamentsClient.findByGtin("03401234567890"))
                .thenReturn(Optional.of(apiResponse));
        when(apiMedicamentMapper.toEntity(any(ApiMedicamentResponse.class)))
                .thenReturn(testMedication);
        when(medicationRepository.save(any(Medication.class)))
                .thenReturn(testMedication);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(eq("SN123456"), eq("03401234567890"), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.AUTHENTIC);
        assertThat(response.getVerificationSource()).isEqualTo("API_MEDICAMENTS_FR");

        // Vérifier que l'API a été appelée et le résultat sauvegardé
        verify(apiMedicamentsClient).findByGtin("03401234567890");
        verify(medicationRepository).save(any(Medication.class));
        Counter successCounter = meterRegistry.counter("external.api.success", "provider", "api-medicaments-fr");
        assertThat(successCounter.count()).isGreaterThan(0);
    }

    @Test
    void verify_InvalidGtin_ShouldReturnUnknown() {
        // Given
        when(medicationRepository.findByGtin("99999999999999"))
                .thenReturn(Optional.empty());
        when(medicationRepository.findByCip13(anyString()))
                .thenReturn(Optional.empty());
        when(apiMedicamentsClient.findByGtin("99999999999999"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        VerificationRequest invalidRequest = VerificationRequest.builder()
                .gtin("99999999999999")
                .build();

        // When
        VerificationResponse response = verificationService.verify(invalidRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.UNKNOWN);
        assertThat(response.getVerificationSource()).isEqualTo("NONE"); // Actually returns NONE when medication is null
        assertThat(response.getMedication()).isNull();
    }

    @Test
    void verify_ExpiredCache_ShouldFetchFromApi() {
        // Given - Cache expiré (updatedAt il y a plus de 30 jours)
        Medication expiredMedication = Medication.builder()
                .id(testMedication.getId())
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .isActive(true)
                .updatedAt(Instant.now().minus(31, java.time.temporal.ChronoUnit.DAYS))
                .build();

        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(expiredMedication));

        ApiMedicamentResponse apiResponse = new ApiMedicamentResponse();
        apiResponse.setGtin("03401234567890");
        apiResponse.setName("Paracétamol 500mg");

        when(apiMedicamentsClient.findByGtin("03401234567890"))
                .thenReturn(Optional.of(apiResponse));
        when(apiMedicamentMapper.toEntity(any(ApiMedicamentResponse.class)))
                .thenReturn(testMedication);
        when(medicationRepository.save(any(Medication.class)))
                .thenReturn(testMedication);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(anyString(), anyString(), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getVerificationSource()).isEqualTo("API_MEDICAMENTS_FR");

        // Vérifier que l'API a été appelée malgré le cache expiré
        verify(apiMedicamentsClient).findByGtin("03401234567890");
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void verify_DuplicateSerialNumber_ShouldReturnSuspicious() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(testMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(eq("SN123456"), eq("03401234567890"), any(), any()))
                .thenReturn(3L); // Au-dessus du seuil (3 utilisateurs)
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.SUSPICIOUS);
        assertThat(response.getConfidence()).isLessThan(0.7);
        assertThat(response.getAlerts()).isNotEmpty();
        assertThat(response.getAlerts()).anyMatch(alert -> 
                "SERIAL_DUPLICATE".equals(alert.getCode()) && 
                "HIGH".equals(alert.getSeverity()));
    }

    @Test
    void verify_ExpiredMedication_ShouldReturnAlert() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(testMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        // Note: countUniqueUsersBySerialAndGtin won't be called if serialNumber is null
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        VerificationRequest expiredRequest = VerificationRequest.builder()
                .gtin("03401234567890")
                .expiryDate(LocalDate.now().minusDays(1)) // Périmé
                .build();

        // When
        VerificationResponse response = verificationService.verify(expiredRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlerts()).isNotEmpty();
        assertThat(response.getAlerts()).anyMatch(alert -> 
                "EXPIRED".equals(alert.getCode()) && 
                "MEDIUM".equals(alert.getSeverity()));
    }

    @Test
    void verify_RecalledBatch_ShouldReturnCriticalAlert() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(testMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(anyString(), anyString(), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        MedicationBatch recalledBatch = MedicationBatch.builder()
                .id(UUID.randomUUID())
                .medication(testMedication)
                .batchNumber("LOT2024A123")
                .isRecalled(true)
                .recallDate(LocalDate.now().minusDays(10))
                .recallReason("Contamination détectée")
                .build();

        when(batchRepository.findByBatchNumberAndMedicationId("LOT2024A123", testMedication.getId()))
                .thenReturn(Optional.of(recalledBatch));

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.SUSPICIOUS);
        assertThat(response.getAlerts()).isNotEmpty();
        assertThat(response.getAlerts()).anyMatch(alert -> 
                "BATCH_RECALLED".equals(alert.getCode()) && 
                "CRITICAL".equals(alert.getSeverity()));
        assertThat(response.getConfidence()).isLessThan(0.5); // Forte pénalité
    }

    @Test
    void verify_InactiveGtin_ShouldReturnHighSeverityAlert() {
        // Given
        Medication inactiveMedication = Medication.builder()
                .id(testMedication.getId())
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .isActive(false) // GTIN inactif
                .updatedAt(Instant.now())
                .build();

        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(inactiveMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(anyString(), anyString(), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.SUSPICIOUS);
        assertThat(response.getAlerts()).isNotEmpty();
        assertThat(response.getAlerts()).anyMatch(alert -> 
                "GTIN_INACTIVE".equals(alert.getCode()) && 
                "HIGH".equals(alert.getSeverity()));
    }

    @Test
    void verify_ApiUnavailable_ShouldFallbackToLocalDb() {
        // Given - Cache vide au premier appel, puis médicament trouvé en fallback
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.empty()) // Cache miss
                .thenReturn(Optional.of(testMedication)); // Fallback: trouve en DB locale
        when(apiMedicamentsClient.findByGtin("03401234567890"))
                .thenThrow(new ExternalApiException("API unavailable"));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(anyString(), anyString(), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getVerificationSource()).isEqualTo("DB_LOCAL_FALLBACK");
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.AUTHENTIC);

        // Vérifier que l'erreur API est enregistrée
        Counter errorCounter = meterRegistry.counter("external.api.error", "provider", "api-medicaments-fr");
        assertThat(errorCounter.count()).isGreaterThan(0);
    }

    @Test
    void verify_ApiError_ShouldFallbackGracefully() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.empty());
        when(medicationRepository.findByCip13(anyString()))
                .thenReturn(Optional.empty());
        when(apiMedicamentsClient.findByGtin("03401234567890"))
                .thenThrow(new ExternalApiException("Timeout"));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getVerificationSource()).isEqualTo("NONE");
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.UNKNOWN);
    }

    @Test
    void verify_SuspiciousMedication_ShouldNotifyAuthorities() {
        // Given
        Medication inactiveMedication = Medication.builder()
                .id(testMedication.getId())
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .isActive(false)
                .updatedAt(Instant.now())
                .build();

        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(inactiveMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(anyString(), anyString(), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        VerificationResponse response = verificationService.verify(testRequest, "test@example.com");

        // Then
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.SUSPICIOUS);
        
        // Vérifier que l'email d'alerte a été envoyé
        verify(emailService).sendSuspiciousMedicationAlert(
                eq("Paracétamol 500mg"),
                eq("03401234567890"),
                anyString()
        );
    }

    @Test
    void verify_ShouldSaveScanHistory() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(testMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(eq("SN123456"), eq("03401234567890"), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        ArgumentCaptor<ScanHistory> scanHistoryCaptor = ArgumentCaptor.forClass(ScanHistory.class);

        // When
        verificationService.verify(testRequest, "test@example.com");

        // Then
        verify(scanHistoryRepository).save(scanHistoryCaptor.capture());
        ScanHistory savedScan = scanHistoryCaptor.getValue();
        
        assertThat(savedScan).isNotNull();
        assertThat(savedScan.getGtin()).isEqualTo("03401234567890");
        assertThat(savedScan.getSerialNumber()).isEqualTo("SN123456");
        assertThat(savedScan.getBatchNumber()).isEqualTo("LOT2024A123");
        assertThat(savedScan.getStatus()).isEqualTo(VerificationStatus.AUTHENTIC);
        assertThat(savedScan.getUser()).isEqualTo(testUser);
        assertThat(savedScan.getMedication()).isEqualTo(testMedication);
    }

    @Test
    void verify_ShouldRecordMetrics() {
        // Given
        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(testMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countUniqueUsersBySerialAndGtin(anyString(), anyString(), any(), any()))
                .thenReturn(0L);
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        verificationService.verify(testRequest, "test@example.com");

        // Then
        // Vérifier que les métriques sont enregistrées
        // Le SimpleMeterRegistry enregistre automatiquement les métriques
        assertThat(meterRegistry.getMeters().size()).isGreaterThan(0);
    }

    @Test
    void verify_WithLocation_ShouldSaveLocationInScanHistory() {
        // Given
        VerificationRequest.LocationData location = VerificationRequest.LocationData.builder()
                .latitude(11.8636)
                .longitude(-15.5984)
                .build();

        VerificationRequest requestWithLocation = VerificationRequest.builder()
                .gtin("03401234567890")
                .location(location)
                .build();

        when(medicationRepository.findByGtin("03401234567890"))
                .thenReturn(Optional.of(testMedication));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        // Pas de serialNumber, donc le count ne sera pas appelé avec serialNumber
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        ArgumentCaptor<ScanHistory> scanHistoryCaptor = ArgumentCaptor.forClass(ScanHistory.class);

        // When
        verificationService.verify(requestWithLocation, "test@example.com");

        // Then
        verify(scanHistoryRepository).save(scanHistoryCaptor.capture());
        ScanHistory savedScan = scanHistoryCaptor.getValue();
        org.locationtech.jts.geom.Point savedLocation = savedScan.getLocation();
        assertThat(savedLocation).isNotNull();
        assertThat(savedLocation.getY()).isCloseTo(11.8636, offset(0.0001)); // latitude
        assertThat(savedLocation.getX()).isCloseTo(-15.5984, offset(0.0001)); // longitude
    }
}

