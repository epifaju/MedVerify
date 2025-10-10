package com.medverify.integration;

import com.medverify.entity.Medication;
import com.medverify.integration.dto.BDPMMedicamentResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir les réponses de l'API BDPM en entités Medication
 */
@Component
public class BDPMMedicamentMapper {

    private static final DateTimeFormatter BDPM_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Convertit une réponse BDPM en entité Medication
     * 
     * @param bdpmResponse Réponse de l'API BDPM
     * @return Entité Medication prête à être sauvegardée
     */
    public Medication toEntity(BDPMMedicamentResponse bdpmResponse) {
        if (bdpmResponse == null) {
            return null;
        }

        // Extraire la première présentation pour obtenir le CIP13
        String cip13 = null;
        String gtin = null;

        if (bdpmResponse.getPresentation() != null && !bdpmResponse.getPresentation().isEmpty()) {
            BDPMMedicamentResponse.PresentationDto firstPresentation = bdpmResponse.getPresentation().get(0);
            if (firstPresentation.getCip13() != null) {
                cip13 = String.valueOf(firstPresentation.getCip13());
                // Construire le GTIN en ajoutant 0 devant le CIP13
                gtin = "0" + cip13;
            }
        }

        // Construire la posologie (basique pour l'instant)
        Medication.PosologyInfo posology = Medication.PosologyInfo.builder()
                .specialInstructions("Consultez la notice pour la posologie complète")
                .build();

        return Medication.builder()
                // Identifiants
                .gtin(gtin)
                .cip13(cip13)
                .cis(bdpmResponse.getCis())

                // Informations de base
                .name(bdpmResponse.getElementPharmaceutique())
                .genericName(extractGenericName(bdpmResponse.getElementPharmaceutique()))
                .manufacturer(bdpmResponse.getTitulaire())
                .manufacturerCountry(null) // BDPM ne fournit pas le pays de fabrication
                .dosage(extractDosage(bdpmResponse.getElementPharmaceutique()))
                .pharmaceuticalForm(bdpmResponse.getFormePharmaceutique())
                .registrationNumber("CIS-" + bdpmResponse.getCis())

                // Informations médicales (basiques)
                .indications(extractIndications(bdpmResponse))
                .posology(posology)
                .sideEffects(List.of()) // Pas fourni par l'API
                .contraindications(extractContraindications(bdpmResponse))

                // Statut
                .isActive(isActive(bdpmResponse))
                .requiresPrescription(requiresPrescription(bdpmResponse))
                .isEssential(false)

                // Métadonnées
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Extrait le nom générique du nom complet
     */
    private String extractGenericName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }

        // Le nom générique est généralement le premier mot ou avant la virgule
        String[] parts = fullName.split("[,\\d]");
        if (parts.length > 0) {
            return parts[0].trim();
        }

        return fullName;
    }

    /**
     * Extrait le dosage du nom complet
     * Ex: "DOLIPRANE 1000 mg, comprimé" → "1000 mg"
     */
    private String extractDosage(String fullName) {
        if (fullName == null) {
            return null;
        }

        // Chercher un pattern de dosage (nombre + unité)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?\\s*(?:mg|g|ml|µg|UI|%))");
        java.util.regex.Matcher matcher = pattern.matcher(fullName);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Extrait les indications depuis la composition
     */
    private List<String> extractIndications(BDPMMedicamentResponse bdpmResponse) {
        List<String> indications = new ArrayList<>();

        // Extraire les substances actives comme indications générales
        if (bdpmResponse.getComposition() != null) {
            bdpmResponse.getComposition().stream()
                    .filter(comp -> "SA".equals(comp.getNatureComposant())) // Substance Active
                    .map(BDPMMedicamentResponse.CompositionDto::getDenominationSubstance)
                    .distinct()
                    .limit(5)
                    .forEach(substance -> indications.add("Contient " + substance));
        }

        return indications;
    }

    /**
     * Extrait les contre-indications depuis les conditions
     */
    private List<String> extractContraindications(BDPMMedicamentResponse bdpmResponse) {
        if (bdpmResponse.getConditions() == null) {
            return List.of();
        }

        // Les conditions sont maintenant directement des strings
        return bdpmResponse.getConditions().stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Détermine si le médicament est actif
     */
    private boolean isActive(BDPMMedicamentResponse bdpmResponse) {
        if (bdpmResponse.getStatusAutorisation() == null) {
            return true;
        }

        String status = bdpmResponse.getStatusAutorisation().toLowerCase();
        return status.contains("active") || status.contains("autorisée") || status.contains("autorisation active");
    }

    /**
     * Détermine si le médicament nécessite une prescription
     */
    private boolean requiresPrescription(BDPMMedicamentResponse bdpmResponse) {
        if (bdpmResponse.getConditions() == null || bdpmResponse.getConditions().isEmpty()) {
            return false; // Par défaut non si pas d'info
        }

        // Chercher des conditions indiquant une prescription
        // Les conditions sont maintenant directement des strings
        return bdpmResponse.getConditions().stream()
                .anyMatch(cond -> cond != null &&
                        (cond.toLowerCase().contains("prescription") ||
                                cond.toLowerCase().contains("ordonnance") ||
                                cond.toLowerCase().contains("liste i") ||
                                cond.toLowerCase().contains("liste ii")));
    }
}
