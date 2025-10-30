package com.medverify.controller.admin;

import com.medverify.dto.request.CreatePharmacyRequest;
import com.medverify.dto.request.PharmacyAdminSearchRequest;
import com.medverify.dto.request.UpdatePharmacyRequest;
import com.medverify.dto.response.PharmacyAdminDTO;
import com.medverify.dto.response.PharmacyDTO;
import com.medverify.dto.response.PharmacyStatsResponse;
import com.medverify.dto.response.PhotoUploadResponse;
import com.medverify.exception.InvalidRequestException;
import com.medverify.service.CloudStorageService;
import com.medverify.service.admin.PharmacyAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Contrôleur admin pour gestion CRUD des pharmacies
 */
@RestController
@RequestMapping("/api/v1/admin/pharmacies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Pharmacies", description = "Endpoints d'administration pour la gestion des pharmacies")
public class PharmacyAdminController {

    private final PharmacyAdminService pharmacyAdminService;
    private final CloudStorageService cloudStorageService;

    /**
     * Créer une nouvelle pharmacie
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Créer une nouvelle pharmacie")
    public ResponseEntity<PharmacyDTO> createPharmacy(
            @Valid @RequestBody CreatePharmacyRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("Creating pharmacy: {} by user: {}", request.getName(), currentUser.getUsername());

        PharmacyDTO pharmacy = pharmacyAdminService.createPharmacy(request, currentUser.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacy);
    }

    /**
     * Modifier une pharmacie
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY', 'PHARMACIST')")
    @Operation(summary = "Modifier une pharmacie")
    public ResponseEntity<PharmacyDTO> updatePharmacy(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePharmacyRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("Updating pharmacy: {} by user: {}", id, currentUser.getUsername());

        PharmacyDTO pharmacy = pharmacyAdminService.updatePharmacy(id, request, currentUser.getUsername());

        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Supprimer (désactiver) une pharmacie
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Supprimer une pharmacie (désactivation)")
    public ResponseEntity<Void> deletePharmacy(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("Deleting pharmacy: {} by user: {}", id, currentUser.getUsername());

        pharmacyAdminService.deletePharmacy(id, currentUser.getUsername());

        return ResponseEntity.noContent().build();
    }

    /**
     * Valider/Vérifier une pharmacie
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Valider ou invalider une pharmacie")
    public ResponseEntity<PharmacyDTO> verifyPharmacy(
            @PathVariable UUID id,
            @RequestParam boolean verified,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("Verifying pharmacy: {} = {} by user: {}", id, verified, currentUser.getUsername());

        PharmacyDTO pharmacy = pharmacyAdminService.verifyPharmacy(id, verified);

        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Upload photo pharmacie
     */
    @PostMapping("/{id}/photo")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY', 'PHARMACIST')")
    @Operation(summary = "Uploader une photo de pharmacie")
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("Uploading photo for pharmacy: {} by user: {}", id, currentUser.getUsername());

        // Valider fichier
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) { // Max 5MB
            throw new InvalidRequestException("Fichier invalide ou trop volumineux (max 5MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidRequestException("Le fichier doit être une image");
        }

        try {
            // Upload vers cloud storage
            String photoUrl = cloudStorageService.uploadFile(
                    file.getBytes(),
                    "pharmacies/" + id + "/" + UUID.randomUUID() + ".jpg",
                    contentType);

            // Mettre à jour pharmacie
            pharmacyAdminService.updatePharmacyPhoto(id, photoUrl);

            return ResponseEntity.ok(new PhotoUploadResponse(photoUrl));

        } catch (Exception e) {
            log.error("Error uploading photo", e);
            throw new RuntimeException("Erreur lors de l'upload", e);
        }
    }

    /**
     * Lister toutes les pharmacies (admin)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Lister toutes les pharmacies avec filtres")
    public ResponseEntity<Page<PharmacyAdminDTO>> getAllPharmacies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search) {

        PharmacyAdminSearchRequest searchRequest = PharmacyAdminSearchRequest.builder()
                .page(page)
                .size(size)
                .city(city)
                .region(region)
                .isVerified(isVerified)
                .isActive(isActive)
                .search(search)
                .build();

        Page<PharmacyAdminDTO> pharmacies = pharmacyAdminService.searchPharmacies(searchRequest);

        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir statistiques pharmacies
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Obtenir les statistiques des pharmacies")
    public ResponseEntity<PharmacyStatsResponse> getPharmacyStats() {
        PharmacyStatsResponse stats = pharmacyAdminService.getPharmacyStats();
        return ResponseEntity.ok(stats);
    }
}
