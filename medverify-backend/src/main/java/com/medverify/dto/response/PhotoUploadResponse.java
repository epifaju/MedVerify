package com.medverify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour réponse d'upload de photo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadResponse {
    private String photoUrl;
}




