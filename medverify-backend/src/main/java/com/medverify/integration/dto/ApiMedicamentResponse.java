package com.medverify.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour la réponse de l'API-Medicaments.fr
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMedicamentResponse {

    @JsonProperty("code")
    private String gtin;

    @JsonProperty("denomination")
    private String name;

    @JsonProperty("forme")
    private String pharmaceuticalForm;

    @JsonProperty("dosage")
    private String dosage;

    @JsonProperty("laboratoire")
    private String manufacturer;

    @JsonProperty("statut")
    private String status; // "Autorisée", "Abrogée", etc.

    @JsonProperty("dateAMM")
    private LocalDate marketingAuthorizationDate;

    @JsonProperty("cis")
    private String cisCode; // Code CIS (identifiant unique français)

    @JsonProperty("substances")
    private List<String> activeSubstances;

    @JsonProperty("compositionTexte")
    private String composition;

    @JsonProperty("indications")
    private String indications;

    @JsonProperty("posologie")
    private String posology;

    @JsonProperty("contrindications")
    private String contraindications;

    @JsonProperty("effetsIndesirables")
    private String sideEffects;

    @JsonProperty("presentations")
    private List<PresentationDto> presentations;

    /**
     * Classe interne pour les présentations
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PresentationDto {
        @JsonProperty("cip13")
        private String cip13;

        @JsonProperty("libelle")
        private String label;

        @JsonProperty("prix")
        private Double price;

        @JsonProperty("remboursable")
        private Boolean reimbursable;
    }
}

