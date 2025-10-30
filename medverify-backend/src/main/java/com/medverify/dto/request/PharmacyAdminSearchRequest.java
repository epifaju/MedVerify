package com.medverify.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour recherche avancée de pharmacies (admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyAdminSearchRequest {

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    private String city;
    private String region;
    private Boolean isVerified;
    private Boolean isActive;
    private String search;
}


