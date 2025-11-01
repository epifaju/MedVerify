package com.medverify.dto.request;

import com.medverify.entity.OpeningHours;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour créer une nouvelle pharmacie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePharmacyRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255)
    private String name;

    @Size(max = 20)
    private String licenseNumber;

    @Pattern(regexp = "^\\+?[0-9\\s-]{8,20}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;

    @Email
    private String email;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 500)
    private String address;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    private String postalCode;

    @NotBlank(message = "La région est obligatoire")
    private String region;

    private String district;

    @NotBlank
    @Builder.Default
    private String country = "Guinée-Bissau";

    // Coordonnées GPS
    @NotNull(message = "La latitude est obligatoire")
    @Min(-90)
    @Max(90)
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    @Min(-180)
    @Max(180)
    private Double longitude;

    // Horaires d'ouverture (JSON)
    private OpeningHours openingHours;

    // Type
    @Builder.Default
    private Boolean is24h = false;

    @Builder.Default
    private Boolean isNightPharmacy = false;

    @Builder.Default
    private Boolean acceptsEmergencies = true;

    // Services
    private List<String> services;

    // Propriétaire (ID utilisateur pharmacien)
    private UUID ownerId;

    // Description
    @Size(max = 1000)
    private String description;

    private String websiteUrl;
}




