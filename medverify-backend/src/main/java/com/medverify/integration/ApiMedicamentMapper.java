package com.medverify.integration;

import com.medverify.entity.Medication;
import com.medverify.integration.dto.ApiMedicamentResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Mapper pour convertir les réponses de l'API-Medicaments.fr en entités
 * Medication
 */
@Component
public class ApiMedicamentMapper {

    /**
     * Convertit la réponse API en entité Medication pour sauvegarde en base
     * 
     * @param apiResponse Réponse de l'API-Medicaments.fr
     * @return Entité Medication prête à être sauvegardée
     */
    public Medication toEntity(ApiMedicamentResponse apiResponse) {
        if (apiResponse == null) {
            return null;
        }

        // Construire la posologie
        Medication.PosologyInfo posology = null;
        if (apiResponse.getPosology() != null && !apiResponse.getPosology().isEmpty()) {
            posology = Medication.PosologyInfo.builder()
                    .adult(apiResponse.getPosology())
                    .specialInstructions(extractSpecialInstructions(apiResponse.getPosology()))
                    .build();
        }

        return Medication.builder()
                // Informations de base
                .gtin(apiResponse.getGtin())
                .name(apiResponse.getName() != null ? apiResponse.getName() : "Nom inconnu")
                .genericName(extractGenericName(apiResponse.getName()))
                .manufacturer(apiResponse.getManufacturer())
                .manufacturerCountry("France")
                .dosage(apiResponse.getDosage())
                .pharmaceuticalForm(apiResponse.getPharmaceuticalForm())
                .registrationNumber(apiResponse.getCisCode())

                // Informations médicales
                .indications(parseIndications(apiResponse.getIndications()))
                .posology(posology)
                .sideEffects(parseSideEffects(apiResponse.getSideEffects()))
                .contraindications(parseContraindications(apiResponse.getContraindications()))

                // Statut et métadonnées
                .isActive(isStatusActive(apiResponse.getStatus()))
                .requiresPrescription(true) // Par défaut pour médicaments français
                .isEssential(false) // À déterminer manuellement
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Parse les indications depuis le texte de l'API
     */
    private List<String> parseIndications(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        // Split par point, virgule ou point-virgule
        return Arrays.stream(text.split("[.;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.length() > 5) // Filtrer les fragments trop courts
                .limit(10) // Limiter à 10 indications principales
                .toList();
    }

    /**
     * Parse les effets indésirables depuis le texte de l'API
     */
    private List<String> parseSideEffects(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(text.split("[.;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.length() > 5)
                .limit(15) // Plus d'effets secondaires possibles
                .toList();
    }

    /**
     * Parse les contre-indications depuis le texte de l'API
     */
    private List<String> parseContraindications(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(text.split("[.;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.length() > 5)
                .limit(10)
                .toList();
    }

    /**
     * Extrait le nom générique du nom complet du médicament
     * Ex: "DOLIPRANE 1000 mg, comprimé" -> "DOLIPRANE"
     */
    private String extractGenericName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }

        // Le nom générique est généralement avant la première virgule ou le premier
        // chiffre
        String[] parts = fullName.split("[,\\d]");
        if (parts.length > 0) {
            return parts[0].trim();
        }

        return fullName;
    }

    /**
     * Extrait les instructions spéciales de la posologie
     */
    private String extractSpecialInstructions(String posology) {
        if (posology == null || posology.isEmpty()) {
            return null;
        }

        // Rechercher des mots-clés d'instructions spéciales
        if (posology.toLowerCase().contains("à jeun") ||
                posology.toLowerCase().contains("pendant les repas") ||
                posology.toLowerCase().contains("au coucher")) {

            // Extraire la partie contenant ces instructions
            String[] sentences = posology.split("\\.");
            for (String sentence : sentences) {
                if (sentence.toLowerCase().contains("à jeun") ||
                        sentence.toLowerCase().contains("pendant les repas") ||
                        sentence.toLowerCase().contains("au coucher")) {
                    return sentence.trim();
                }
            }
        }

        return null;
    }

    /**
     * Détermine si le statut indique un médicament actif
     */
    private boolean isStatusActive(String status) {
        if (status == null) {
            return true; // Par défaut actif si pas d'info
        }

        String statusLower = status.toLowerCase();
        return statusLower.contains("autorisée") ||
                statusLower.contains("autorisé") ||
                statusLower.contains("active") ||
                statusLower.contains("valide");
    }
}




