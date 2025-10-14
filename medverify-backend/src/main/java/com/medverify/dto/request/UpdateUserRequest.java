package com.medverify.dto.request;

import com.medverify.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour mettre à jour un utilisateur
 */
@Data
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    private UserRole role;

    private Boolean isActive;
    private Boolean isVerified;
}



