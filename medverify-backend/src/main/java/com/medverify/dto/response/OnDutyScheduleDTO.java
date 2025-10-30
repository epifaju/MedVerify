package com.medverify.dto.response;

import com.medverify.entity.DutyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO pour représenter un planning de garde
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnDutyScheduleDTO {

    private UUID id;
    private UUID pharmacyId;
    private String pharmacyName;

    private LocalDate startDate;
    private LocalDate endDate;

    private DutyType dutyType;

    private LocalTime startTime;
    private LocalTime endTime;

    private String region;
    private String district;
    private String notes;

    private Boolean isActive;

    private Instant createdAt;
    private String createdBy;
}
