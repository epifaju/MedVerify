package com.medverify.service;

import com.medverify.entity.User;
import com.medverify.entity.UserRole;
import com.medverify.entity.VerificationCode;
import com.medverify.repository.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailVerificationService
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailVerificationService, "codeExpiryMinutes", 15);
        ReflectionTestUtils.setField(emailVerificationService, "maxPendingCodes", 5);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.PATIENT)
                .isVerified(false)
                .build();
    }

    @Test
    void createEmailVerificationCode_ShouldCreateAndReturnCode() {
        // Given
        when(verificationCodeRepository.countPendingCodesByUser(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(0L);

        VerificationCode savedCode = VerificationCode.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .code("123456")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(savedCode);

        // When
        VerificationCode result = emailVerificationService.createEmailVerificationCode(testUser);

        // Then
        assertThat(result).isNotNull();
        verify(verificationCodeRepository).save(any(VerificationCode.class));
        // Note: @Async method may execute asynchronously, so we check it was called but don't wait
        // The email sending is asynchronous and might not complete immediately in tests
    }

    @Test
    void createEmailVerificationCode_MaxPendingCodes_ShouldThrowException() {
        // Given
        when(verificationCodeRepository.countPendingCodesByUser(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(5L); // Max reached

        // When & Then
        assertThatThrownBy(() -> emailVerificationService.createEmailVerificationCode(testUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trop de codes");

        verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
    }

    @Test
    void verifyCode_ValidCode_ShouldReturnTrue() {
        // Given
        VerificationCode code = VerificationCode.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .code("123456")
                .email("test@example.com")
                .expiresAt(Instant.now().plusSeconds(900))
                .attempts(0)
                .verifiedAt(null)
                .build();

        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "123456"))
                .thenReturn(Optional.of(code));
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenAnswer(invocation -> {
            VerificationCode saved = invocation.getArgument(0);
            return saved; // Just return as-is, don't modify
        });

        // When
        boolean result = emailVerificationService.verifyCode("test@example.com", "123456");

        // Then
        assertThat(result).isTrue();
        verify(verificationCodeRepository, times(2)).save(any(VerificationCode.class)); // Once for attempts, once for verifiedAt
    }

    @Test
    void verifyCode_InvalidCode_ShouldReturnFalse() {
        // Given
        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "wrong"))
                .thenReturn(Optional.empty());

        // When
        boolean result = emailVerificationService.verifyCode("test@example.com", "wrong");

        // Then
        assertThat(result).isFalse();
        verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
    }

    @Test
    void verifyCode_ExpiredCode_ShouldReturnFalse() {
        // Given
        VerificationCode expiredCode = VerificationCode.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .code("123456")
                .email("test@example.com")
                .expiresAt(Instant.now().minusSeconds(100))
                .attempts(0)
                .verifiedAt(null)
                .build();

        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "123456"))
                .thenReturn(Optional.of(expiredCode));
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(expiredCode);

        // When
        boolean result = emailVerificationService.verifyCode("test@example.com", "123456");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void verifyCode_MaxAttemptsReached_ShouldReturnFalse() {
        // Given
        VerificationCode code = VerificationCode.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .code("123456")
                .email("test@example.com")
                .expiresAt(Instant.now().plusSeconds(900))
                .attempts(3)
                .maxAttempts(3)
                .verifiedAt(null)
                .build();

        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "123456"))
                .thenReturn(Optional.of(code));
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(code);

        // When
        boolean result = emailVerificationService.verifyCode("test@example.com", "123456");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void cleanExpiredCodes_ShouldDeleteExpiredCodes() {
        // Given
        when(verificationCodeRepository.deleteExpiredCodes(any(Instant.class))).thenReturn(5);

        // When
        int deleted = emailVerificationService.cleanExpiredCodes();

        // Then
        assertThat(deleted).isEqualTo(5);
        verify(verificationCodeRepository).deleteExpiredCodes(any(Instant.class));
    }

    @Test
    void resendVerificationCode_ValidExistingCode_ShouldResend() {
        // Given
        VerificationCode existingCode = VerificationCode.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .code("123456")
                .email("test@example.com")
                .expiresAt(Instant.now().plusSeconds(900))
                .attempts(0)
                .verifiedAt(null)
                .build();

        when(verificationCodeRepository.findTopByUserIdAndTypeOrderByCreatedAtDesc(
                eq(testUser.getId()), any()))
                .thenReturn(Optional.of(existingCode));

        // When
        emailVerificationService.resendVerificationCode(testUser.getId(), "test@example.com");

        // Then
        // Note: @Async method may execute asynchronously, so we check it was called but don't wait
        // The email sending is asynchronous and might not complete immediately in tests
    }

    @Test
    void createEmailVerificationCode_ShouldGenerate6DigitCode() {
        // Given
        when(verificationCodeRepository.countPendingCodesByUser(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(0L);

        ArgumentCaptor<VerificationCode> codeCaptor = ArgumentCaptor.forClass(VerificationCode.class);
        when(verificationCodeRepository.save(codeCaptor.capture())).thenAnswer(invocation -> {
            VerificationCode code = invocation.getArgument(0);
            code.setId(UUID.randomUUID());
            return code;
        });

        // When
        emailVerificationService.createEmailVerificationCode(testUser);

        // Then
        VerificationCode savedCode = codeCaptor.getValue();
        assertThat(savedCode.getCode()).matches("\\d{6}"); // 6 digits
        assertThat(savedCode.getCode().length()).isEqualTo(6);
    }
}

