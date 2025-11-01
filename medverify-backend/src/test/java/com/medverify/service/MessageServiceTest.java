package com.medverify.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour MessageService
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        // Reset locale
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void get_ValidKey_ShouldReturnMessage() {
        // Given
        String key = "auth.login.success";
        String expectedMessage = "Connexion réussie";
        when(messageSource.getMessage(anyString(), any(Object[].class), anyString(), any(Locale.class)))
                .thenReturn(expectedMessage);

        // When
        String result = messageService.get(key);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void get_WithParameters_ShouldReturnFormattedMessage() {
        // Given
        String key = "bdpm.import.progress";
        String expectedMessage = "Import terminé: 1000 médicaments";
        when(messageSource.getMessage(anyString(), any(Object[].class), anyString(), any(Locale.class)))
                .thenReturn(expectedMessage);

        // When
        String result = messageService.get(key, 1000);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void get_WithLocale_ShouldUseSpecificLocale() {
        // Given
        String key = "test.key";
        Locale locale = Locale.forLanguageTag("pt");
        String expectedMessage = "Mensagem em português";
        when(messageSource.getMessage(anyString(), any(Object[].class), anyString(), eq(locale)))
                .thenReturn(expectedMessage);

        // When
        String result = messageService.get(key, locale, "param1");

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void getCurrentLocale_ShouldReturnCurrentLocale() {
        // Given
        Locale.setDefault(Locale.FRENCH);
        LocaleContextHolder.setLocale(Locale.FRENCH);

        // When
        Locale result = messageService.getCurrentLocale();

        // Then
        assertThat(result).isEqualTo(Locale.FRENCH);
    }

    @Test
    void getCurrentLanguage_ShouldReturnLanguageCode() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("pt"));

        // When
        String result = messageService.getCurrentLanguage();

        // Then
        assertThat(result).isEqualTo("pt");
    }

    @Test
    void get_MissingKey_ShouldReturnKey() {
        // Given
        String key = "missing.key";
        when(messageSource.getMessage(anyString(), any(Object[].class), anyString(), any(Locale.class)))
                .thenAnswer(invocation -> {
                    // Return the default message (the key itself)
                    return invocation.getArgument(2);
                });

        // When
        String result = messageService.get(key);

        // Then
        assertThat(result).isEqualTo(key);
    }

    @Test
    void get_WithMultipleParameters_ShouldHandleAll() {
        // Given
        String key = "test.multiple";
        String expectedMessage = "Test with A, B, C";
        when(messageSource.getMessage(anyString(), any(Object[].class), anyString(), any(Locale.class)))
                .thenReturn(expectedMessage);

        // When
        String result = messageService.get(key, "A", "B", "C");

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void getCurrentLanguage_PortugueseLocale_ShouldReturnPt() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("pt-PT"));

        // When
        String result = messageService.getCurrentLanguage();

        // Then
        assertThat(result).isEqualTo("pt");
    }

    @Test
    void getCurrentLocale_DefaultLocale_ShouldReturnDefault() {
        // Given
        LocaleContextHolder.resetLocaleContext();

        // When
        Locale result = messageService.getCurrentLocale();

        // Then - Should return system default or context default
        assertThat(result).isNotNull();
    }
}

