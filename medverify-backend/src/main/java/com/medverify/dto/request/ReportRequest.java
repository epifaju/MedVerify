package com.medverify.dto.request;

import com.medverify.entity.ReportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour la requête de création de signalement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    private UUID medicationId;

    private String gtin;

    private String serialNumber;

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotNull(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    private PurchaseLocationData purchaseLocation;

    private List<String> photoUrls;

    @Builder.Default
    private Boolean anonymous = false;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseLocationData {
        private String name;
        private String address;
        private String city;
        private String region;
        private Double latitude;
        private Double longitude;
    }
}
