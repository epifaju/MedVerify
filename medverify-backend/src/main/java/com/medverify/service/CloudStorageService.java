package com.medverify.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service pour gérer l'upload de fichiers vers cloud storage
 * 
 * Implémentation actuelle :
 * - Stockage local : Fonctionnel (développement/test)
 * - S3 (AWS) : Non implémenté - utilise fallback local
 * 
 * TODO Futures améliorations (Issue #TBD) :
 * - Implémenter upload S3 avec AWS SDK v2
 * - Implémenter upload Google Cloud Storage (optionnel)
 * - Implémenter upload Azure Blob Storage (optionnel)
 * - Ajouter support CDN pour fichiers uploadés
 * - Ajouter validation de taille/type de fichier
 * 
 * @see <a href="https://github.com/medverify/medverify-backend/issues">Issues GitHub</a>
 */
@Service
@Slf4j
public class CloudStorageService {

    @Value("${cloud-storage.provider:local}")
    private String provider;

    @Value("${cloud-storage.bucket-name:medverify-uploads}")
    private String bucketName;

    @Value("${cloud-storage.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Upload fichier vers cloud storage
     * 
     * @param fileData Contenu du fichier en bytes
     * @param fileName Nom du fichier
     * @param contentType Type MIME du fichier
     * @return URL publique du fichier uploadé
     */
    public String uploadFile(byte[] fileData, String fileName, String contentType) {

        if ("s3".equalsIgnoreCase(provider)) {
            return uploadToS3(fileData, fileName, contentType);
        } else {
            return uploadToLocal(fileData, fileName);
        }
    }

    /**
     * Upload vers S3 (AWS SDK)
     * 
     * ⚠️ NON IMPLÉMENTÉ - Utilise fallback vers stockage local
     * 
     * Pour implémenter :
     * 1. Ajouter dépendance Maven : aws-java-sdk-s3
     * 2. Configurer credentials AWS (variables d'environnement ou IAM role)
     * 3. Décommenter et adapter le code ci-dessous
     * 
     * Code d'exemple (à adapter) :
     * <pre>
     * AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
     * ObjectMetadata metadata = new ObjectMetadata();
     * metadata.setContentType(contentType);
     * metadata.setContentLength(fileData.length);
     * s3Client.putObject(bucketName, fileName, new ByteArrayInputStream(fileData), metadata);
     * return s3Client.getUrl(bucketName, fileName).toString();
     * </pre>
     * 
     * @see <a href="https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/s3-examples.html">AWS SDK S3 Documentation</a>
     */
    private String uploadToS3(byte[] fileData, String fileName, String contentType) {
        log.warn("S3 upload not implemented (provider: {}), using local storage fallback", provider);
        log.info("To implement S3 upload, see CloudStorageService.uploadToS3() documentation");
        return uploadToLocal(fileData, fileName);
    }

    /**
     * Upload vers système de fichiers local (pour développement)
     */
    private String uploadToLocal(byte[] fileData, String fileName) {
        try {
            // Créer dossier uploads si n'existe pas
            Path uploadDir = Paths.get("uploads/pharmacies");
            Files.createDirectories(uploadDir);

            // Sauvegarder fichier
            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, fileData);

            // Retourner URL relative
            return baseUrl + "/uploads/pharmacies/" + fileName;

        } catch (IOException e) {
            log.error("Error uploading file locally: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'upload du fichier", e);
        }
    }

    /**
     * Supprimer fichier
     * 
     * ⚠️ IMPLÉMENTATION PARTIELLE - Seulement pour stockage local
     * 
     * TODO Futures améliorations (Issue #TBD) :
     * - Implémenter suppression S3
     * - Implémenter suppression autres providers
     * - Ajouter validation de l'URL
     * - Ajouter logging d'audit
     * 
     * @param fileUrl URL du fichier à supprimer
     */
    public void deleteFile(String fileUrl) {
        if ("s3".equalsIgnoreCase(provider)) {
            log.warn("S3 delete not implemented, skipping deletion of: {}", fileUrl);
            return;
        }
        
        // Implémentation pour stockage local
        try {
            // Extraire le nom du fichier de l'URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get("uploads/pharmacies", fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileName);
            } else {
                log.warn("File not found for deletion: {}", fileName);
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            // Ne pas lever d'exception pour éviter de bloquer l'application
        }
    }
}
