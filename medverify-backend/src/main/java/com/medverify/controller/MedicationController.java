package com.medverify.controller;

import com.medverify.dto.request.VerificationRequest;
import com.medverify.dto.response.VerificationResponse;
import com.medverify.entity.Medication;
import com.medverify.repository.MedicationRepository;
import com.medverify.service.MedicationVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur pour les médicaments et la vérification
 */
@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
@Tag(name = "Medications", description = "Endpoints for medication verification and management")
public class MedicationController {

    private final MedicationVerificationService verificationService;
    private final MedicationRepository medicationRepository;

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN')")
    @Operation(summary = "Verify medication authenticity")
    public ResponseEntity<VerificationResponse> verify(
            @Valid @RequestBody VerificationRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        VerificationResponse response = verificationService.verify(request, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get medication details by ID")
    public ResponseEntity<Medication> getMedicationById(@PathVariable UUID id) {
        return medicationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN')")
    @Operation(summary = "Search medications by name")
    public ResponseEntity<List<Medication>> searchMedications(@RequestParam String name) {
        List<Medication> medications = medicationRepository.searchByName(name);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/essential")
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get essential medications")
    public ResponseEntity<List<Medication>> getEssentialMedications() {
        List<Medication> medications = medicationRepository.findByIsEssentialTrue();
        return ResponseEntity.ok(medications);
    }
}

