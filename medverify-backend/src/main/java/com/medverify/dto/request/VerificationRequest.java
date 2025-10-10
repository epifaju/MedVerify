package com.medverify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour la requête de vérification de médicament
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {

    @NotBlank(message = "GTIN is required")
    @Pattern(regexp = "\\d{13,14}", message = "GTIN must be 13 or 14 digits")
    private String gtin;

    private String serialNumber;

    private String batchNumber;

    private LocalDate expiryDate;

    private LocationData location;

    private DeviceInfo deviceInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private Double latitude;
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        private String platform;
        private String osVersion;
        private String appVersion;
        private String deviceModel;
    }
}

