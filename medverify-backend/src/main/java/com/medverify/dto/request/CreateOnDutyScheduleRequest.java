package com.medverify.dto.request;

import com.medverify.entity.DutyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO pour créer une garde de pharmacie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOnDutyScheduleRequest {

    @NotNull(message = "L'ID de la pharmacie est obligatoire")
    private UUID pharmacyId;

    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début doit être aujourd'hui ou future")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être future")
    private LocalDate endDate;

    @NotNull(message = "Le type de garde est obligatoire")
    private DutyType dutyType;

    private LocalTime startTime;
    private LocalTime endTime;

    @NotBlank(message = "La région est obligatoire")
    private String region;

    private String district;

    @Size(max = 500)
    private String notes;

    // Validation custom : endDate >= startDate
    @AssertTrue(message = "La date de fin doit être >= date de début")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null)
            return true;
        return !endDate.isBefore(startDate);
    }
}




