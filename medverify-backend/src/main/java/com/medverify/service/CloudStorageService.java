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
 * Pour MVP : implémenter stockage local ou S3
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
     * Pour MVP : implémenter stockage local ou S3
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
     */
    private String uploadToS3(byte[] fileData, String fileName, String contentType) {
        // TODO: Implémenter avec AWS SDK S3
        // AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setContentType(contentType);
        // s3Client.putObject(bucketName, fileName, new ByteArrayInputStream(fileData),
        // metadata);
        // return s3Client.getUrl(bucketName, fileName).toString();

        log.warn("S3 upload not implemented, using local storage");
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
     */
    public void deleteFile(String fileUrl) {
        // TODO: Implémenter suppression selon provider
        log.info("Delete file: {}", fileUrl);
    }
}
