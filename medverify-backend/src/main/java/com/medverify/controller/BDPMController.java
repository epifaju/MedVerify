package com.medverify.controller;

import com.medverify.dto.response.MessageResponse;
import com.medverify.service.BDPMImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion de l'import BDPM
 */
@RestController
@RequestMapping("/api/v1/admin/bdpm")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BDPM Admin", description = "Administration de l'import BDPM")
@SecurityRequirement(name = "bearerAuth")
public class BDPMController {

    private final BDPMImportService bdpmImportService;

    /**
     * Déclenche l'import manuel de la base BDPM
     * Réservé aux administrateurs
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import manuel de la base BDPM", description = "Télécharge et importe les 15K+ médicaments depuis l'API BDPM. Opération longue (~5-10 minutes).")
    public ResponseEntity<MessageResponse> triggerImport() {
        log.info("Manual BDPM import triggered by admin");

        // Lancer l'import dans un thread séparé pour ne pas bloquer la requête
        new Thread(() -> {
            try {
                bdpmImportService.importFullBDPM();
            } catch (Exception e) {
                log.error("Error during manual BDPM import: {}", e.getMessage(), e);
            }
        }).start();

        return ResponseEntity.accepted()
                .body(MessageResponse.of(
                        "Import BDPM démarré. Consultez les logs pour suivre la progression. " +
                                "L'import peut prendre 5-10 minutes."));
    }

    /**
     * Obtient le statut de l'import BDPM
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Statut de l'import BDPM")
    public ResponseEntity<?> getImportStatus() {
        // Pour l'instant, retourner un message simple
        // À améliorer avec un vrai statut (progress, last import, etc.)
        return ResponseEntity.ok()
                .body(MessageResponse.of("Import service operational. Check logs for details."));
    }
}
