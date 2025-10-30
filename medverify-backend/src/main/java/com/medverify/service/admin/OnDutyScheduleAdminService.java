package com.medverify.service.admin;

import com.medverify.dto.request.CreateOnDutyScheduleRequest;
import com.medverify.dto.request.UpdateOnDutyScheduleRequest;
import com.medverify.dto.response.OnDutyScheduleDTO;
import com.medverify.entity.OnDutySchedule;
import com.medverify.entity.Pharmacy;
import com.medverify.entity.User;
import com.medverify.exception.InvalidRequestException;
import com.medverify.exception.ResourceNotFoundException;
import com.medverify.repository.OnDutyScheduleRepository;
import com.medverify.repository.PharmacyRepository;
import com.medverify.repository.UserRepository;
import com.medverify.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service admin pour gestion des plannings de garde
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OnDutyScheduleAdminService {

    private final OnDutyScheduleRepository onDutyScheduleRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /**
     * Créer une garde
     */
    public OnDutyScheduleDTO createSchedule(CreateOnDutyScheduleRequest request, String createdBy) {
        // Vérifier que la pharmacie existe
        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));

        if (!pharmacy.getIsActive()) {
            throw new InvalidRequestException("La pharmacie doit être active");
        }

        // Vérifier chevauchement de gardes pour cette pharmacie
        List<OnDutySchedule> existingSchedules = onDutyScheduleRepository
                .findOverlappingSchedules(
                        request.getPharmacyId(),
                        request.getStartDate(),
                        request.getEndDate());

        if (!existingSchedules.isEmpty()) {
            throw new InvalidRequestException(
                    "Cette pharmacie a déjà une garde pendant cette période");
        }

        // Récupérer utilisateur créateur
        User creator = userRepository.findByEmail(createdBy)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Créer garde
        OnDutySchedule schedule = OnDutySchedule.builder()
                .pharmacy(pharmacy)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dutyType(request.getDutyType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .region(request.getRegion())
                .district(request.getDistrict())
                .notes(request.getNotes())
                .isActive(true)
                .createdAt(java.time.Instant.now())
                .createdBy(creator)
                .build();

        schedule = onDutyScheduleRepository.save(schedule);

        // Audit log
        auditLogService.log(
                createdBy,
                "DUTY_SCHEDULE_CREATE",
                "OnDutySchedule",
                schedule.getId(),
                null,
                schedule);

        log.info("On-duty schedule created: {}", schedule.getId());

        return convertToDTO(schedule);
    }

    /**
     * Modifier une garde
     */
    public OnDutyScheduleDTO updateSchedule(UUID scheduleId, UpdateOnDutyScheduleRequest request) {
        OnDutySchedule schedule = onDutyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Garde non trouvée"));

        // Mettre à jour champs
        if (request.getPharmacyId() != null) {
            Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));
            schedule.setPharmacy(pharmacy);
        }

        if (request.getStartDate() != null)
            schedule.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)
            schedule.setEndDate(request.getEndDate());
        if (request.getDutyType() != null)
            schedule.setDutyType(request.getDutyType());
        if (request.getStartTime() != null)
            schedule.setStartTime(request.getStartTime());
        if (request.getEndTime() != null)
            schedule.setEndTime(request.getEndTime());
        if (request.getRegion() != null)
            schedule.setRegion(request.getRegion());
        if (request.getDistrict() != null)
            schedule.setDistrict(request.getDistrict());
        if (request.getNotes() != null)
            schedule.setNotes(request.getNotes());
        if (request.getIsActive() != null)
            schedule.setIsActive(request.getIsActive());

        schedule = onDutyScheduleRepository.save(schedule);

        log.info("On-duty schedule updated: {}", scheduleId);

        return convertToDTO(schedule);
    }

    /**
     * Supprimer une garde
     */
    public void deleteSchedule(UUID scheduleId) {
        OnDutySchedule schedule = onDutyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Garde non trouvée"));

        // Soft delete
        schedule.setIsActive(false);
        onDutyScheduleRepository.save(schedule);

        log.info("On-duty schedule deleted: {}", scheduleId);
    }

    /**
     * Rechercher gardes
     */
    @Transactional(readOnly = true)
    public Page<OnDutyScheduleDTO> searchSchedules(
            int page, int size, String region,
            LocalDate startDate, LocalDate endDate,
            com.medverify.entity.DutyType dutyType, Boolean isActive) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());

        Specification<OnDutySchedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (region != null) {
                predicates.add(cb.equal(root.get("region"), region));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            if (dutyType != null) {
                predicates.add(cb.equal(root.get("dutyType"), dutyType));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OnDutySchedule> schedulesPage = onDutyScheduleRepository.findAll(spec, pageable);

        return schedulesPage.map(this::convertToDTO);
    }

    /**
     * Obtenir gardes d'une pharmacie
     */
    @Transactional(readOnly = true)
    public List<OnDutyScheduleDTO> getSchedulesByPharmacy(UUID pharmacyId) {
        List<OnDutySchedule> schedules = onDutyScheduleRepository
                .findByPharmacyIdOrderByStartDateDesc(pharmacyId);

        return schedules.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Convertir Entity -> DTO
    private OnDutyScheduleDTO convertToDTO(OnDutySchedule schedule) {
        return OnDutyScheduleDTO.builder()
                .id(schedule.getId())
                .pharmacyId(schedule.getPharmacy().getId())
                .pharmacyName(schedule.getPharmacy().getName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .dutyType(schedule.getDutyType())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .region(schedule.getRegion())
                .district(schedule.getDistrict())
                .notes(schedule.getNotes())
                .isActive(schedule.getIsActive())
                .createdAt(schedule.getCreatedAt())
                .createdBy(schedule.getCreatedBy() != null ? schedule.getCreatedBy().getEmail() : null)
                .build();
    }
}
