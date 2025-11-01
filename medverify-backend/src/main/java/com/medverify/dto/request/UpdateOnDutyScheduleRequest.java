package com.medverify.dto.request;

import com.medverify.entity.DutyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO pour mettre à jour une garde de pharmacie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOnDutyScheduleRequest {

    private UUID pharmacyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private DutyType dutyType;
    private LocalTime startTime;
    private LocalTime endTime;
    private String region;
    private String district;
    private String notes;
    private Boolean isActive;
}




