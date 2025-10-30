package com.medverify.controller.admin;

import com.medverify.dto.request.CreateOnDutyScheduleRequest;
import com.medverify.dto.request.UpdateOnDutyScheduleRequest;
import com.medverify.dto.response.OnDutyScheduleDTO;
import com.medverify.entity.DutyType;
import com.medverify.service.admin.OnDutyScheduleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur admin pour gestion des plannings de garde
 */
@RestController
@RequestMapping("/api/v1/admin/on-duty-schedules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - On-Duty Schedules", description = "Endpoints d'administration pour la gestion des gardes")
public class OnDutyScheduleAdminController {

    private final OnDutyScheduleAdminService onDutyScheduleAdminService;

    /**
     * Créer une garde
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Créer un planning de garde")
    public ResponseEntity<OnDutyScheduleDTO> createSchedule(
            @Valid @RequestBody CreateOnDutyScheduleRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("Creating on-duty schedule for pharmacy: {} by user: {}",
                request.getPharmacyId(), currentUser.getUsername());

        OnDutyScheduleDTO schedule = onDutyScheduleAdminService.createSchedule(
                request,
                currentUser.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    /**
     * Modifier une garde
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Modifier un planning de garde")
    public ResponseEntity<OnDutyScheduleDTO> updateSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOnDutyScheduleRequest request) {

        log.info("Updating on-duty schedule: {}", id);

        OnDutyScheduleDTO schedule = onDutyScheduleAdminService.updateSchedule(id, request);

        return ResponseEntity.ok(schedule);
    }

    /**
     * Supprimer une garde
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Supprimer un planning de garde")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) {

        log.info("Deleting on-duty schedule: {}", id);

        onDutyScheduleAdminService.deleteSchedule(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Lister toutes les gardes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Lister toutes les gardes avec filtres")
    public ResponseEntity<Page<OnDutyScheduleDTO>> getAllSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) DutyType dutyType,
            @RequestParam(required = false) Boolean isActive) {

        Page<OnDutyScheduleDTO> schedules = onDutyScheduleAdminService.searchSchedules(
                page, size, region, startDate, endDate, dutyType, isActive);

        return ResponseEntity.ok(schedules);
    }

    /**
     * Obtenir gardes d'une pharmacie
     */
    @GetMapping("/pharmacy/{pharmacyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY', 'PHARMACIST')")
    @Operation(summary = "Obtenir les gardes d'une pharmacie")
    public ResponseEntity<List<OnDutyScheduleDTO>> getPharmacySchedules(
            @PathVariable UUID pharmacyId) {

        List<OnDutyScheduleDTO> schedules = onDutyScheduleAdminService
                .getSchedulesByPharmacy(pharmacyId);

        return ResponseEntity.ok(schedules);
    }
}
