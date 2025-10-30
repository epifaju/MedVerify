package com.medverify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO pour statistiques des pharmacies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyStatsResponse {

    private Long totalPharmacies;
    private Long activePharmacies;
    private Long verifiedPharmacies;
    private Long pharmacies24h;
    private Long nightPharmacies;

    private Map<String, Long> pharmaciesByRegion;
}


