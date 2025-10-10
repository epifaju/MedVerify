package com.medverify.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse de l'API BDPM (medicamentsapi.giygas.dev)
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BDPMMedicamentResponse {

    @JsonProperty("cis")
    private Long cis; // Code Identifiant Spécialité

    @JsonProperty("elementPharmaceutique")
    private String elementPharmaceutique; // Nom complet

    @JsonProperty("formePharmaceutique")
    private String formePharmaceutique;

    @JsonProperty("voiesAdministration")
    private List<String> voiesAdministration;

    @JsonProperty("statusAutorisation")
    private String statusAutorisation;

    @JsonProperty("typeProcedure")
    private String typeProcedure;

    @JsonProperty("etatComercialisation")
    private String etatComercialisation;

    @JsonProperty("dateAMM")
    private String dateAMM; // Format: DD/MM/YYYY

    @JsonProperty("titulaire")
    private String titulaire; // Fabricant

    @JsonProperty("surveillanceRenforce")
    private String surveillanceRenforce;

    @JsonProperty("composition")
    private List<CompositionDto> composition;

    @JsonProperty("generiques")
    private List<GeneriqueDto> generiques;

    @JsonProperty("presentation")
    private List<PresentationDto> presentation;

    @JsonProperty("conditions")
    private List<String> conditions; // C'est un tableau de strings, pas d'objets

    /**
     * Composition du médicament
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompositionDto {
        @JsonProperty("cis")
        private Long cis;

        @JsonProperty("elementPharmaceutique")
        private String elementPharmaceutique;

        @JsonProperty("codeSubstance")
        private Integer codeSubstance;

        @JsonProperty("denominationSubstance")
        private String denominationSubstance;

        @JsonProperty("dosage")
        private String dosage;

        @JsonProperty("referenceDosage")
        private String referenceDosage;

        @JsonProperty("natureComposant")
        private String natureComposant; // SA = Substance Active
    }

    /**
     * Informations génériques
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeneriqueDto {
        @JsonProperty("group")
        private Integer group; // Champ "group" dans l'API, pas "groupID"

        @JsonProperty("libelle")
        private String libelle;

        @JsonProperty("type")
        private String type; // "Princeps" ou "Générique"
    }

    /**
     * Présentation commerciale (avec CIP13)
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PresentationDto {
        @JsonProperty("cis")
        private Long cis;

        @JsonProperty("cip7")
        private Long cip7;

        @JsonProperty("cip13")
        private Long cip13; // ← IMPORTANT : Correspond au GTIN (13 derniers chiffres)

        @JsonProperty("libelle")
        private String libelle;

        @JsonProperty("statusAdministratif")
        private String statusAdministratif;

        @JsonProperty("etatComercialisation")
        private String etatComercialisation;

        @JsonProperty("dateDeclaration")
        private String dateDeclaration;

        @JsonProperty("agreement")
        private String agreement;

        @JsonProperty("tauxRemboursement")
        private String tauxRemboursement;

        @JsonProperty("prix")
        private Double prix;
    }
}
