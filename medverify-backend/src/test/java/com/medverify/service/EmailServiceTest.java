package com.medverify.service;

import com.medverify.entity.User;
import com.medverify.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailService
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@medverify.com");
        ReflectionTestUtils.setField(emailService, "emailPassword", "password123");

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void sendEmail_EmailConfigured_ShouldSendEmail() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendEmail("test@example.com", "Subject", "Body");

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("test@example.com");
        assertThat(message.getSubject()).isEqualTo("Subject");
        assertThat(message.getText()).isEqualTo("Body");
        assertThat(message.getFrom()).isEqualTo("noreply@medverify.com");
    }

    @Test
    void sendEmail_EmailNotConfigured_ShouldNotSend() {
        // Given
        ReflectionTestUtils.setField(emailService, "fromEmail", "");
        ReflectionTestUtils.setField(emailService, "emailPassword", "");

        // When
        emailService.sendEmail("test@example.com", "Subject", "Body");

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_ExceptionThrown_ShouldPropagate() {
        // Given
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                emailService.sendEmail("test@example.com", "Subject", "Body"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email");
    }

    @Test
    void sendVerificationEmail_ShouldSendVerificationEmail() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendVerificationEmail(testUser);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("test@example.com");
        assertThat(message.getSubject()).isEqualTo("MedVerify - Verify Your Account");
        assertThat(message.getText()).contains("John Doe");
        assertThat(message.getFrom()).isEqualTo("noreply@medverify.com");
    }

    @Test
    void sendVerificationEmail_EmailNotConfigured_ShouldNotSend() {
        // Given
        ReflectionTestUtils.setField(emailService, "fromEmail", null);

        // When
        emailService.sendVerificationEmail(testUser);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSuspiciousMedicationAlert_ShouldSendAlert() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendSuspiciousMedicationAlert("Paracétamol", "03401234567890", "Bissau");

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getSubject()).isEqualTo("MedVerify - Suspicious Medication Alert");
        assertThat(message.getText()).contains("Paracétamol");
        assertThat(message.getText()).contains("03401234567890");
        assertThat(message.getText()).contains("Bissau");
    }

    @Test
    void sendWelcomeEmail_ShouldSendWelcomeEmail() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendWelcomeEmail(testUser, "TempPassword123");

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("test@example.com");
        assertThat(message.getSubject()).isEqualTo("MedVerify - Bienvenue !");
        assertThat(message.getText()).contains("John");
        assertThat(message.getText()).contains("TempPassword123");
    }

    @Test
    void sendPasswordResetEmail_ShouldSendResetEmail() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendPasswordResetEmail(testUser, "NewPassword123");

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("test@example.com");
        assertThat(message.getSubject()).isEqualTo("MedVerify - Réinitialisation de mot de passe");
        assertThat(message.getText()).contains("NewPassword123");
    }

    @Test
    void sendSuspiciousMedicationAlert_Exception_ShouldLogAndNotThrow() {
        // Given
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then - Should not throw
        emailService.sendSuspiciousMedicationAlert("Paracétamol", "03401234567890", "Bissau");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}

