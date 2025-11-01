package com.medverify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medverify.dto.request.VerificationRequest;
import com.medverify.dto.response.VerificationResponse;
import com.medverify.entity.Medication;
import com.medverify.entity.VerificationStatus;
import com.medverify.repository.MedicationRepository;
import com.medverify.service.MedicationVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour MedicationController
 */
@WebMvcTest(MedicationController.class)
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicationVerificationService verificationService;

    @MockBean
    private MedicationRepository medicationRepository;

    @Test
    @WithMockUser(roles = "PATIENT")
    void verify_AuthenticatedUser_ShouldReturnVerificationResponse() throws Exception {
        // Given
        VerificationRequest request = VerificationRequest.builder()
                .gtin("03401234567890")
                .serialNumber("SN123456")
                .build();

        VerificationResponse response = VerificationResponse.builder()
                .status(VerificationStatus.AUTHENTIC)
                .confidence(0.95)
                .verificationSource("CACHE_LOCAL")
                .build();

        when(verificationService.verify(any(VerificationRequest.class), any(String.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/medications/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHENTIC"))
                .andExpect(jsonPath("$.confidence").value(0.95));
    }

    @Test
    void verify_Unauthenticated_ShouldReturn401() throws Exception {
        // Given
        VerificationRequest request = VerificationRequest.builder()
                .gtin("03401234567890")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/medications/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getById_ValidId_ShouldReturnMedication() throws Exception {
        // Given
        UUID medicationId = UUID.randomUUID();
        Medication medication = Medication.builder()
                .id(medicationId)
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .build();

        when(medicationRepository.findById(medicationId))
                .thenReturn(Optional.of(medication));

        // When & Then
        mockMvc.perform(get("/api/v1/medications/" + medicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(medicationId.toString()))
                .andExpect(jsonPath("$.gtin").value("03401234567890"))
                .andExpect(jsonPath("$.name").value("Paracétamol 500mg"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getById_InvalidId_ShouldReturn404() throws Exception {
        // Given
        UUID invalidId = UUID.randomUUID();
        when(medicationRepository.findById(invalidId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/medications/" + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void search_ValidName_ShouldReturnList() throws Exception {
        // Given
        Medication medication1 = Medication.builder()
                .id(UUID.randomUUID())
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .build();

        Medication medication2 = Medication.builder()
                .id(UUID.randomUUID())
                .gtin("03401234567891")
                .name("Paracétamol 1000mg")
                .build();

        List<Medication> medications = List.of(medication1, medication2);

        when(medicationRepository.searchByName("Paracétamol"))
                .thenReturn(medications);

        // When & Then
        mockMvc.perform(get("/api/v1/medications/search")
                        .param("name", "Paracétamol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Paracétamol 500mg"))
                .andExpect(jsonPath("$[1].name").value("Paracétamol 1000mg"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getEssentialMedications_ShouldReturnList() throws Exception {
        // Given
        Medication essential1 = Medication.builder()
                .id(UUID.randomUUID())
                .gtin("03401234567890")
                .name("Paracétamol 500mg")
                .isEssential(true)
                .build();

        List<Medication> essentialMedications = List.of(essential1);

        when(medicationRepository.findByIsEssentialTrue())
                .thenReturn(essentialMedications);

        // When & Then
        mockMvc.perform(get("/api/v1/medications/essential"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isEssential").value(true));
    }
}

