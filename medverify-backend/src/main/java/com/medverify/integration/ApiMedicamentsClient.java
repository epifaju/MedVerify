package com.medverify.integration;

import com.medverify.exception.ExternalApiException;
import com.medverify.integration.dto.ApiMedicamentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Client pour l'API-Medicaments.fr
 * Permet de rechercher des médicaments français par code GTIN
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiMedicamentsClient {

    @Value("${external-api.medicaments.base-url:https://api-medicaments.fr}")
    private String baseUrl;

    @Value("${external-api.medicaments.timeout:5000}")
    private int timeout;

    @Value("${external-api.medicaments.enabled:true}")
    private boolean enabled;

    private final RestTemplate restTemplate;

    /**
     * Recherche un médicament par code GTIN
     * 
     * @param gtin Code GTIN à 13 ou 14 chiffres
     * @return Optional contenant la réponse API si trouvé, vide sinon
     * @throws ExternalApiException si l'API est inaccessible ou en erreur
     */
    public Optional<ApiMedicamentResponse> findByGtin(String gtin) {
        if (!enabled) {
            log.info("External API is disabled, skipping API call");
            return Optional.empty();
        }

        try {
            // Normaliser le GTIN (ajouter zéros si nécessaire pour avoir 14 chiffres)
            String normalizedGtin = normalizeGtin(gtin);

            String url = baseUrl + "/medicaments?gtin=" + normalizedGtin;
            log.info("Calling API-Medicaments.fr: {}", url);

            // Préparer les headers pour forcer JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "MedVerify/1.0");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Appel API avec headers
            ResponseEntity<ApiMedicamentResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ApiMedicamentResponse[].class);

            // Vérifier que la réponse n'est pas null
            if (response == null) {
                log.error("API response is null for GTIN: {}", gtin);
                throw new ExternalApiException("API-Medicaments.fr returned null response");
            }

            if (response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().length > 0) {

                log.info("Medication found in API-Medicaments.fr for GTIN: {}", gtin);
                return Optional.of(response.getBody()[0]);
            }

            log.warn("No medication found in API-Medicaments.fr for GTIN: {}", gtin);
            return Optional.empty();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Medication not found (404) for GTIN: {}", gtin);
            return Optional.empty();

        } catch (HttpClientErrorException e) {
            // 4xx erreurs (autres que 404) sont des erreurs client
            log.error("Client error calling API-Medicaments.fr for GTIN {}: {} - {}",
                    gtin, e.getStatusCode(), e.getMessage());

            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiException("Rate limit exceeded for API-Medicaments.fr", e);
            }

            return Optional.empty();

        } catch (Exception e) {
            // Erreurs réseau, timeout, 5xx
            log.error("Error calling API-Medicaments.fr for GTIN {}: {}", gtin, e.getMessage(), e);
            throw new ExternalApiException("API-Medicaments.fr unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Normalise le GTIN pour avoir exactement 14 chiffres
     * Ex: "3400930485088" -> "03400930485088"
     * 
     * @param gtin GTIN à normaliser
     * @return GTIN à 14 chiffres avec padding de zéros à gauche
     */
    private String normalizeGtin(String gtin) {
        if (gtin == null || gtin.isEmpty()) {
            return "";
        }

        // Supprimer espaces, tirets et autres caractères non numériques
        String clean = gtin.replaceAll("[^0-9]", "");

        // Padding avec zéros à gauche si < 14 caractères
        while (clean.length() < 14) {
            clean = "0" + clean;
        }

        // Tronquer si > 14 (cas improbable mais sécurité)
        if (clean.length() > 14) {
            clean = clean.substring(clean.length() - 14);
        }

        return clean;
    }

    /**
     * Vérifie si l'API externe est disponible
     * 
     * @return true si l'API répond, false sinon
     */
    public boolean isAvailable() {
        if (!enabled) {
            return false;
        }

        try {
            String healthUrl = baseUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("API-Medicaments.fr health check failed: {}", e.getMessage());
            return false;
        }
    }
}
