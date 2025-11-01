package com.medverify.controller;

import com.medverify.dto.request.LoginRequest;
import com.medverify.dto.request.RefreshTokenRequest;
import com.medverify.dto.request.RegisterRequest;
import com.medverify.dto.request.ResendCodeRequest;
import com.medverify.dto.request.VerifyEmailRequest;
import com.medverify.dto.response.AuthResponse;
import com.medverify.dto.response.MessageResponse;
import com.medverify.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour l'authentification
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Créer un nouveau compte utilisateur. Un email de vérification sera envoyé."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authentifier un utilisateur et obtenir un access token et refresh token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "423", description = "Account locked (too many failed attempts)")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Obtenir un nouveau access token à partir d'un refresh token valide."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify email with 6-digit code", description = "Verifies user email and activates the account")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        MessageResponse response = authService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-code")
    @Operation(summary = "Resend verification code", description = "Resends the verification code to user's email")
    public ResponseEntity<MessageResponse> resendCode(@Valid @RequestBody ResendCodeRequest request) {
        MessageResponse response = authService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(response);
    }
}
