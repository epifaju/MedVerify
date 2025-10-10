package com.medverify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO générique pour les réponses de message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }
}

