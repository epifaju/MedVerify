package com.medverify.service;

import com.medverify.dto.request.ReportRequest;
import com.medverify.dto.response.ReportResponse;
import com.medverify.entity.*;
import com.medverify.exception.ResourceNotFoundException;
import com.medverify.repository.MedicationRepository;
import com.medverify.repository.ReportRepository;
import com.medverify.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ReportService
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private Medication testMedication;
    private Report testReport;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.PATIENT)
                .build();

        testMedication = Medication.builder()
                .id(UUID.randomUUID())
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .build();

        testReport = Report.builder()
                .id(UUID.randomUUID())
                .reporter(testUser)
                .medication(testMedication)
                .gtin("03401234567890")
                .serialNumber("SN123456")
                .reportType(ReportType.COUNTERFEIT)
                .description("Médicament suspect")
                .status(ReportStatus.SUBMITTED)
                .referenceNumber("REP-2024-ABC123")
                .anonymous(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void createReport_ValidRequest_ShouldCreateReport() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .gtin("03401234567890")
                .serialNumber("SN123456")
                .reportType(ReportType.COUNTERFEIT)
                .description("Médicament suspect")
                .anonymous(false)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(medicationRepository.findByGtin("03401234567890")).thenReturn(Optional.of(testMedication));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setReferenceNumber("REP-2024-ABC123");
            saved.setCreatedAt(Instant.now());
            return saved;
        });

        // When
        ReportResponse response = reportService.createReport(request, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReferenceNumber()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ReportStatus.SUBMITTED);
        verify(reportRepository).save(any(Report.class));
        verify(emailService).sendSuspiciousMedicationAlert(anyString(), anyString(), anyString());
    }

    @Test
    void createReport_WithMedicationId_ShouldUseMedication() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .medicationId(testMedication.getId())
                .reportType(ReportType.COUNTERFEIT)
                .description("Médicament suspect")
                .anonymous(false)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(medicationRepository.findById(testMedication.getId())).thenReturn(Optional.of(testMedication));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setReferenceNumber("REP-2024-ABC123");
            saved.setCreatedAt(Instant.now());
            return saved;
        });

        // When
        ReportResponse response = reportService.createReport(request, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        verify(medicationRepository).findById(testMedication.getId());
    }

    @Test
    void createReport_Anonymous_ShouldNotSendConfirmationEmail() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .gtin("03401234567890")
                .reportType(ReportType.COUNTERFEIT)
                .description("Médicament suspect")
                .anonymous(true)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(medicationRepository.findByGtin("03401234567890")).thenReturn(Optional.of(testMedication));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setReferenceNumber("REP-2024-ABC123");
            saved.setCreatedAt(Instant.now());
            return saved;
        });

        // When
        reportService.createReport(request, "test@example.com");

        // Then
        verify(reportRepository).save(any(Report.class));
        verify(emailService).sendSuspiciousMedicationAlert(anyString(), anyString(), anyString());
    }

    @Test
    void createReport_UserNotFound_ShouldThrowException() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .gtin("03401234567890")
                .reportType(ReportType.COUNTERFEIT)
                .build();

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportService.createReport(request, "unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getReportById_ValidId_ShouldReturnReport() {
        // Given
        UUID reportId = testReport.getId();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));

        // When
        ReportResponse.ReportDetailsResponse result = reportService.getReportById(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reportId);
        assertThat(result.getReferenceNumber()).isEqualTo("REP-2024-ABC123");
        assertThat(result.getGtin()).isEqualTo("03401234567890");
    }

    @Test
    void getReportById_InvalidId_ShouldThrowException() {
        // Given
        UUID invalidId = UUID.randomUUID();
        when(reportRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportService.getReportById(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Report not found");
    }

    @Test
    void getUserReports_ShouldReturnUserReports() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(reportRepository.findByReporterIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(List.of(testReport));

        // When
        List<ReportResponse.ReportDetailsResponse> result = reportService.getUserReports("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getGtin()).isEqualTo("03401234567890");
    }

    @Test
    void getReportsByStatus_ShouldReturnPaginatedReports() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(List.of(testReport), pageable, 1);
        when(reportRepository.findByStatus(ReportStatus.SUBMITTED, pageable)).thenReturn(reportPage);

        // When
        Page<ReportResponse.ReportDetailsResponse> result = reportService.getReportsByStatus(
                ReportStatus.SUBMITTED, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateReportStatus_ValidRequest_ShouldUpdateStatus() {
        // Given
        UUID reportId = testReport.getId();
        User reviewer = User.builder()
                .id(UUID.randomUUID())
                .email("reviewer@example.com")
                .role(UserRole.AUTHORITY)
                .build();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
        when(userRepository.findByEmail("reviewer@example.com")).thenReturn(Optional.of(reviewer));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReportResponse.ReportDetailsResponse result = reportService.updateReportStatus(
                reportId, ReportStatus.CONFIRMED, "Médicament contrefait confirmé", "reviewer@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReportStatus.CONFIRMED);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void updateReportStatus_AnonymousReport_ShouldNotSendEmail() {
        // Given
        testReport = Report.builder()
                .id(testReport.getId())
                .reporter(testReport.getReporter())
                .medication(testReport.getMedication())
                .gtin(testReport.getGtin())
                .serialNumber(testReport.getSerialNumber())
                .reportType(testReport.getReportType())
                .description(testReport.getDescription())
                .status(testReport.getStatus())
                .referenceNumber(testReport.getReferenceNumber())
                .anonymous(true)
                .createdAt(testReport.getCreatedAt())
                .build();
        UUID reportId = testReport.getId();
        User reviewer = User.builder()
                .id(UUID.randomUUID())
                .email("reviewer@example.com")
                .role(UserRole.AUTHORITY)
                .build();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
        when(userRepository.findByEmail("reviewer@example.com")).thenReturn(Optional.of(reviewer));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reportService.updateReportStatus(
                reportId, ReportStatus.CONFIRMED, "Confirmed", "reviewer@example.com");

        // Then
        verify(reportRepository).save(any(Report.class));
    }
}

