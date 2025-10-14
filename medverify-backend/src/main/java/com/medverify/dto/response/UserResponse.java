package com.medverify.dto.response;

import com.medverify.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de réponse pour les informations utilisateur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isVerified;
    private Boolean isActive;
    private Integer failedLoginAttempts;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    /**
     * Masque les informations sensibles pour affichage public
     */
    public static UserResponse fromUser(com.medverify.entity.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isVerified(user.getIsVerified())
                .isActive(user.getIsActive())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}



